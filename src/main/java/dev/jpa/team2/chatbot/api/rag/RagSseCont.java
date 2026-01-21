package dev.jpa.team2.chatbot.api.rag;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.*;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import dev.jpa.team2.chatbot.domain.rag.ChatRagDto;
import dev.jpa.team2.chatbot.domain.rag.ChatRagService;
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

    // sessionId 당 1개 제한: sessionId -> activeJobId
    private final ConcurrentHashMap<Long, String> activeJobBySession = new ConcurrentHashMap<>();

    private final ExecutorService genExecutor = Executors.newFixedThreadPool(16);
    private final ExecutorService sseExecutor = Executors.newFixedThreadPool(16);

    // ------------------------------------------------------------
    // start : job 생성 + 백그라운드 생성 작업 시작 + jobId 반환
    // ------------------------------------------------------------
    @PostMapping("/ask/start")
    public ResponseEntity<?> start(@RequestBody ChatRagDto dto, HttpSession session) {
        // 로그인 사용자 확인
        Long memberId = AuthSessionUtil.requireMemberId(session); 
        
        // sessionId 필수 체크
        Long sessionId = dto.getSessionId();  
        if (sessionId == null) {
            return ResponseEntity.badRequest().body("sessionId is required");
        }
        
        // jobId 생성 + Job 생성
        String jobId = UUID.randomUUID().toString();
        RagSseHub.Job job = new RagSseHub.Job(memberId, sessionId);

        // sessionId당 1개 제한: 기존 job 있으면 종료 처리
        String oldJobId = activeJobBySession.put(sessionId, jobId);
        if (oldJobId != null) {
            RagSseHub.Job old = hub.get(oldJobId);
            if (old != null) old.fail(new RuntimeException("replaced_by_new_request"));
            hub.remove(oldJobId);
        }

        hub.put(jobId, job);  // Hub 등록

        // 백그라운드에서 기존 ask() 실행 (DB 저장 등 기존 로직 그대로)
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

        return ResponseEntity.ok(java.util.Map.of("jobId", jobId)); // 즉시 jobId 반환
    }

    // ------------------------------------------------------------
    // stream : SSE 연결 열고 큐에서 chunk 꺼내 스트리밍 (30초 타임아웃 + disconnect 정리)
    // ------------------------------------------------------------
    @GetMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam("jobId") String jobId, HttpSession session) {
        // SseEmitter 생성 (타임아웃 30초)
        final long timeoutMs = 30_000L;
        SseEmitter emitter = new SseEmitter(timeoutMs);
        
        // 권한 체크 + job 존재 확인
        Long memberId = AuthSessionUtil.requireMemberId(session); 
        RagSseHub.Job job = hub.get(jobId);
        if (job == null) {
            completeWithError(emitter, "job_not_found");
            return emitter;
        }
        if (!job.getMemberId().equals(memberId)) {
            completeWithError(emitter, "forbidden");
            return emitter;
        }
        
        // cleanup 정의
        Runnable cleanup = () -> {
            // job이 내 세션의 active job이면 매핑 제거
            Long sessionId = job.getSessionId();
            activeJobBySession.compute(sessionId, (sid, curJobId) -> curJobId != null && curJobId.equals(jobId) ? null : curJobId);

            // job이 완료된 상태면 hub에서도 제거해서 메모리 누수 방지
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
        
        // SSE 전송 루프를 별도 스레드에서 실행
        sseExecutor.submit(() -> {
            try {
                while (true) {
                    // 250ms마다 큐를 체크해서 chunk가 있으면 data(chunk)로 전송
                    String chunk = job.getQueue().poll(250, TimeUnit.MILLISECONDS);
                    if (chunk != null) {
                        emitter.send(SseEmitter.event().data(chunk));
                    }

                    // job이 끝났고 큐도 비었으면 종료
                    if (job.isDone() && job.getQueue().isEmpty()) {
                        if (job.getError() != null) {
                            // 실패면 error 이벤트로 알려줌
                            emitter.send(SseEmitter.event().name("error").data("generation_failed"));
                        } else {
                            // 성공이면 meta 이벤트로 최종 DTO를 JSON으로 보내고, done 이벤트(event name을 "meta")로 마무리
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
    
    // 가짜 스트리밍(타이핑 효과) 생성 : 완성된 답변 문자열을 잘게 쪼개서, 일정 시간 간격으로 SSE 전송 큐에 넣어 “타이핑되는 것처럼 보이게” 만드는 역할
    private void pushChunks(RagSseHub.Job job, String text, int chunkChars, int chunkDelayMs) throws InterruptedException {
        if (text == null) text = "";  // 실패/빈 답변이어도 스트리밍 루프가 안전하게 끝나게 함
        int n = text.length();  // 전체 길이 계산
        for (int i = 0; i < n; i += chunkChars) { // chunkChars 단위로 문자열 자르기
            int end = Math.min(i + chunkChars, n);
            job.push(text.substring(i, end)); // 큐에 push
            Thread.sleep(chunkDelayMs); // 잠깐 쉬기 (타이핑 속도 조절)
        }
    }
    
    // 에러 상황에서 SSE를 정상 종료시키는 유틸 : SSE 연결에서 문제가 생겼을 때, 에러 이벤트를 보내고 스트림을 깔끔하게 종료
    private void completeWithError(SseEmitter emitter, String msg) {
        try {
            emitter.send(SseEmitter.event().name("error").data(msg)); // 에러 이벤트 전송
        } catch (Exception ignore) {}
        try { emitter.complete(); } catch (Exception ignore) {} // SSE 연결 종료
    }
}
