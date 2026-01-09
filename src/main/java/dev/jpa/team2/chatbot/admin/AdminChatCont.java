package dev.jpa.team2.chatbot.admin;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.message.ChatMessage;
import dev.jpa.team2.chatbot.session.ChatSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/chat")
public class AdminChatCont {

    private final AdminChatService adminChatService;

    // ✅ 관리자: 세션 목록(페이지)
    // GET /api/admin/chat/sessions?status=ACTIVE&memberId=23&q=임차&page=0&size=20&from=2026-01-01T00:00:00&to=...
    @GetMapping("/sessions")
    public ResponseEntity<?> listSessions(
        @RequestParam(name="status", required=false) String status,
        @RequestParam(name="memberId", required=false) Long memberId,
        @RequestParam(name="q", required=false) String q,
        @RequestParam(name="from", required=false) LocalDateTime from,
        @RequestParam(name="to", required=false) LocalDateTime to,
        @RequestParam(name="page", defaultValue="0") int page,
        @RequestParam(name="size", defaultValue="20") int size
    ) {
        // ✅ 1) Page<?> 말고 Page<ChatSession>로 받기 (캐스팅 제거)
        Page<ChatSession> p = adminChatService.listSessions(status, memberId, q, from, to, page, size);

        // ✅ 2) 이번 페이지에 있는 sessionId들을 뽑아서
        List<Long> sessionIds = p.getContent().stream()
            .map(ChatSession::getSessionId)
            .collect(Collectors.toList());

        // ✅ 3) 세션별 메시지 카운트를 한 번에 조회 (N+1 방지)
        Map<Long, Long> counts = adminChatService.countMessagesBulk(sessionIds);

        // ✅ 4) content를 DTO(Map)로 변환 (Map.of 금지 -> HashMap 사용)
        List<Map<String, Object>> content = p.getContent().stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", s.getSessionId());
            map.put("memberId", s.getMemberId());
            map.put("title", s.getTitle());
            map.put("status", s.getSessionStatus());
            map.put("startTime", s.getStartTime());
            map.put("lastMessageAt", s.getLastMessageAt());
            map.put("deletedAt", s.getDeletedAt());

            // ✅ 메시지 개수 추가
            map.put("messageCount", counts.getOrDefault(s.getSessionId(), 0L));

            return map;
        }).collect(Collectors.toList());

        // ✅ 5) 응답 Map도 HashMap으로 (Map.of 대신)
        Map<String, Object> out = new HashMap<>();
        out.put("content", content);
        out.put("page", p.getNumber());
        out.put("size", p.getSize());
        out.put("totalElements", p.getTotalElements());
        out.put("totalPages", p.getTotalPages());

        return ResponseEntity.ok(out);
    }

    // ✅ 관리자: 세션 메시지 상세
    // GET /api/admin/chat/sessions/{sessionId}/messages
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<?> sessionMessages(@PathVariable("sessionId") Long sessionId) {
        List<ChatMessage> list = adminChatService.loadMessages(sessionId);

        List<Map<String, Object>> msgs = list.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("chatId", m.getChatId());
            map.put("role", m.getRole());
            map.put("content", m.getContent());
            map.put("createdAt", m.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> out = new HashMap<>();
        out.put("sessionId", sessionId);
        out.put("messages", msgs);

        return ResponseEntity.ok(out);
    }

    // ✅ 관리자: 소프트 삭제
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> softDelete(@PathVariable("sessionId") Long sessionId) {
        adminChatService.softDelete(sessionId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ✅ 관리자: 복구
    @PatchMapping("/sessions/{sessionId}/restore")
    public ResponseEntity<?> restore(@PathVariable("sessionId") Long sessionId) {
        adminChatService.restore(sessionId);
        return ResponseEntity.ok(Map.of("restored", true));
    }

    // ✅ (옵션) 하드 삭제
    @DeleteMapping("/sessions/{sessionId}/hard")
    public ResponseEntity<?> hardDelete(@PathVariable("sessionId") Long sessionId) {
        adminChatService.hardDelete(sessionId);
        return ResponseEntity.ok(Map.of("hardDeleted", true));
    }
}
