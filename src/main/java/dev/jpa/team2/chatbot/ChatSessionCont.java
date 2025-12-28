package dev.jpa.team2.chatbot;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/sessions")
public class ChatSessionCont {

    private final ChatSessionService sessionService;

    // ✅ 미니팝업: 최근 세션(없으면 생성)
    @PostMapping("/latest")
    public ResponseEntity<?> latest(HttpSession httpSession) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);
        ChatSession s = sessionService.getOrCreateLatestSession(memberId);
        return ResponseEntity.ok(java.util.Map.of("sessionId", s.getSessionId()));
    }

    // ✅ aibotpage: 날짜별 세션 리스트
    @GetMapping("/grouped")
    public ResponseEntity<List<GroupedSessionsDto>> grouped(HttpSession httpSession) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);
        return ResponseEntity.ok(sessionService.getGroupedSessions(memberId));
    }

    // ✅ 세션 soft delete
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> delete(@PathVariable Long sessionId, HttpSession httpSession) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);
        sessionService.softDelete(memberId, sessionId);
        return ResponseEntity.ok(java.util.Map.of("deleted", true));
    }
}
