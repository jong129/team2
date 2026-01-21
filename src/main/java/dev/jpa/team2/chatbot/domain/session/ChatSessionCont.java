package dev.jpa.team2.chatbot.domain.session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/sessions")
public class ChatSessionCont {

    private final ChatSessionService sessionService;

    // 회원: 커서 기반 세션 목록 (더보기)
    // GET /api/chat/sessions?cursor=2026-01-06T09:00:00&size=30
    @GetMapping("")
    public ResponseEntity<?> listMySessionsCursor(@RequestParam(name="cursor", required=false)
                                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                     LocalDateTime cursor,
                                                                     @RequestParam(name="size", defaultValue="30") int size,
                                                                     HttpSession httpSession) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);
        Map<String, Object> out = sessionService.listMySessionsCursor(memberId, cursor, size);
        return ResponseEntity.ok(out);
    }

    // 세션 생성
    // POST /api/chat/sessions
    @PostMapping("")
    public ResponseEntity<ChatSessionDto.SessionCreateResponse> create(@RequestBody(required = false) ChatSessionDto.SessionCreateRequest req,
                                                                                                   HttpSession httpSession) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);
        String title = (req == null) ? null : req.getTitle();

        log.info("[ChatSessionCont] create | memberId={} title={}", memberId, title);

        ChatSession s = sessionService.createSession(memberId, title);

        return ResponseEntity.ok(ChatSessionDto.SessionCreateResponse.builder()
            .success(true)
            .sessionId(s.getSessionId())
            .build());
    }

    // 최근 ACTIVE 세션 가져오기 (없으면 생성)
    // POST /api/chat/sessions/latest
    @PostMapping("/latest")
    public ResponseEntity<ChatSessionDto.SessionCreateResponse> latest(HttpSession httpSession) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);

        ChatSession s = sessionService.getOrCreateLatestActiveSession(memberId);

        return ResponseEntity.ok(ChatSessionDto.SessionCreateResponse.builder()
            .success(true)
            .sessionId(s.getSessionId())
            .build());
    }

    // 세션 제목 갱신 API
    @PatchMapping("/{sessionId}/title")
    public ResponseEntity<?> updateTitle(@PathVariable("sessionId") Long sessionId, HttpSession httpSession) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);

        // 제목 갱신 시도
        sessionService.ensureTitleUpdated(memberId, sessionId);

        // 결과 title 반환
        ChatSession s = sessionService.requireOwnedSession(memberId, sessionId);
        return ResponseEntity.ok(Map.of("title", s.getTitle()));
    }

    // 날짜별 세션 그룹
    // GET /api/chat/sessions/grouped
    @GetMapping("/grouped")
    public ResponseEntity<List<ChatSessionDto.GroupedByDate<ChatSessionDto.SessionItem>>> grouped(HttpSession httpSession) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);
        return ResponseEntity.ok(sessionService.getGroupedSessions(memberId));
    }

    // 세션 삭제(소프트)
    // DELETE /api/chat/sessions/{sessionId}
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> delete(@PathVariable("sessionId") Long sessionId, HttpSession httpSession) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);
        sessionService.softDelete(memberId, sessionId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
