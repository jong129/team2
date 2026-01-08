package dev.jpa.team2.chatbot.rag;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.*;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rag")
public class RagSseCont {

    private final ChatRagService chatRagService;
    private final RagSseHub hub;
    private final ObjectMapper objectMapper;

    // ✅ sessionId 당 1개 제한: sessionId -> activeJobId
    private final ConcurrentHashMap<Long, String> activeJobBySession = new ConcurrentHashMap<>();

    private final ExecutorService genExecutor = Executors.newFixedThreadPool(16);
    private final ExecutorService sseExecutor = Executors.newFixedThreadPool(16);

    // ------------------------------------------------------------
    // 1) 질문은 POST로 한번에 전송 -> jobId 리턴
    // ------------------------------------------------------------
    @PostMapping("/ask/start")
    public ResponseEntity<?> start(@RequestBody ChatRagDto dto, HttpSession session) {

        Long memberId = AuthSessionUtil.requireMemberId(session); // ✅ 너희 방식으로 memberId 얻기
        Long sessionId = dto.getSessionId();

        if (sessionId == null) {
            return ResponseEntity.badRequest().body("sessionId is required");
        }

        String jobId = UUID.randomUUID().toString();
        RagSseHub.Job job = new RagSseHub.Job(memberId, sessionId);

        // ✅ sessionId당 1개 제한: 기존 job 있으면 종료 처리
        String oldJobId = activeJobBySession.put(sessionId, jobId);
        if (oldJobId != null) {
            RagSseHub.Job old = hub.get(oldJobId);
            if (old != null) old.fail(new RuntimeException("replaced_by_new_request"));
            hub.remove(oldJobId);
        }

        hub.put(jobId, job);

        // ✅ 백그라운드에서 기존 ask() 실행 (DB 저장 등 기존 로직 그대로)
        genExecutor.submit(() -> {
            try {
                ChatRagDto out = chatRagService.ask(dto, memberId);

                // out.getAnswer()를 50ms/50자 단위로 job 큐에 push
                pushChunks(job, out.getAnswer(), 50, 50);

                // 마지막에 meta도 보낼 수 있게 out 저장하고 완료 처리
                job.complete(out);

            } catch (Exception e) {
                job.fail(e);
            }
        });

        return ResponseEntity.ok(java.util.Map.of("jobId", jobId));
    }

    // ------------------------------------------------------------
    // 2) 답변만 SSE로 스트리밍 (30초 타임아웃 + disconnect 정리)
    // ------------------------------------------------------------
    @GetMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam("jobId") String jobId, HttpSession session) {

        Long memberId = AuthSessionUtil.requireMemberId(session); // ✅ 권한 체크용
        RagSseHub.Job job = hub.get(jobId);

        final long timeoutMs = 30_000L;
        SseEmitter emitter = new SseEmitter(timeoutMs);

        if (job == null) {
            completeWithError(emitter, "job_not_found");
            return emitter;
        }

        // ✅ 내 job인지 확인(최소한의 보안)
        if (!job.getMemberId().equals(memberId)) {
            completeWithError(emitter, "forbidden");
            return emitter;
        }

        Runnable cleanup = () -> {
            // job이 내 세션의 active job이면 매핑 제거
            Long sessionId = job.getSessionId();
            activeJobBySession.compute(sessionId, (sid, curJobId) -> curJobId != null && curJobId.equals(jobId) ? null : curJobId);

            // 완료된 job은 제거
            RagSseHub.Job j = hub.get(jobId);
            if (j != null && j.isDone()) hub.remove(jobId);
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(() -> {
            log.info("[SSE] timeout jobId={}", jobId);
            cleanup.run();
            try { emitter.complete(); } catch (Exception ignore) {}
        });
        emitter.onError(ex -> {
            log.info("[SSE] error jobId={} ex={}", jobId, ex.toString());
            cleanup.run();
        });

        sseExecutor.submit(() -> {
            try {
                while (true) {
                    // 큐에서 chunk를 기다렸다가 보내기
                    String chunk = job.getQueue().poll(250, TimeUnit.MILLISECONDS);
                    if (chunk != null) {
                        emitter.send(SseEmitter.event().data(chunk));
                    }

                    // 완료면 종료
                    if (job.isDone() && job.getQueue().isEmpty()) {
                        if (job.getError() != null) {
                            emitter.send(SseEmitter.event().name("error").data("generation_failed"));
                        } else {
                            // ✅ 마지막에 meta(참고문헌/후속질문/assistantChatId)도 보내고 싶으면
                            //    event name을 "meta"로 JSON 보내기
                            ChatRagDto finalDto = job.getFinalDto();
                            if (finalDto != null) {
                              String metaJson = objectMapper.writeValueAsString(finalDto);
                              emitter.send(SseEmitter.event().name("meta").data(metaJson));
                            }
                            emitter.send(SseEmitter.event().name("done").data("ok"));
                        }
                        emitter.complete();
                        break;
                    }
                }
            } catch (IOException io) {
                // 브라우저 닫힘 등 disconnect
                log.info("[SSE] client disconnected jobId={}", jobId);
                try { emitter.complete(); } catch (Exception ignore) {}
            } catch (Exception e) {
                log.error("[SSE] stream loop failed jobId={}", jobId, e);
                try { emitter.completeWithError(e); } catch (Exception ignore) {}
            } finally {
                cleanup.run();
            }
        });

        return emitter;
    }

    private void pushChunks(RagSseHub.Job job, String text, int chunkChars, int chunkDelayMs) throws InterruptedException {
        if (text == null) text = "";
        int n = text.length();
        for (int i = 0; i < n; i += chunkChars) {
            int end = Math.min(i + chunkChars, n);
            job.push(text.substring(i, end));
            Thread.sleep(chunkDelayMs);
        }
    }

    private void completeWithError(SseEmitter emitter, String msg) {
        try {
            emitter.send(SseEmitter.event().name("error").data(msg));
        } catch (Exception ignore) {}
        try { emitter.complete(); } catch (Exception ignore) {}
    }
}
