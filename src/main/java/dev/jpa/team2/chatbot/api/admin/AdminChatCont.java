package dev.jpa.team2.chatbot.api.admin;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.domain.message.ChatMessage;
import dev.jpa.team2.chatbot.domain.session.ChatSession;
import lombok.RequiredArgsConstructor;

// 관리자 채팅 운영 API 엔드포인트를 제공하고, 서비스 결과를 관리자 화면에 맞는 JSON 형태로 변환해 응답하는 컨트롤러

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/chat")
public class AdminChatCont {

    private final AdminChatService adminChatService;

    // 세션 목록 (검색 + 페이지 + 메시지 수 포함. 페이지)
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
        // 세션 페이지 조회
        Page<ChatSession> p = adminChatService.listSessions(status, memberId, q, from, to, page, size);

        // sessionId 추출
        List<Long> sessionIds = p.getContent().stream()
            .map(ChatSession::getSessionId)
            .collect(Collectors.toList());

        // 메시지 count 벌크 조회 (N+1 방지)
        Map<Long, Long> counts = adminChatService.countMessagesBulk(sessionIds);

        // 응답 조립 (세션 row + messageCount) : content를 DTO(Map)로 변환 (Map.of 금지 -> HashMap 사용)
        List<Map<String, Object>> content = p.getContent().stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", s.getSessionId());
            map.put("memberId", s.getMemberId());
            map.put("title", s.getTitle());
            map.put("status", s.getSessionStatus());
            map.put("startTime", s.getStartTime());
            map.put("lastMessageAt", s.getLastMessageAt());
            map.put("deletedAt", s.getDeletedAt());
            map.put("messageCount", counts.getOrDefault(s.getSessionId(), 0L)); // 메시지 개수 추가

            return map;
        }).collect(Collectors.toList());

        // 페이징 메타 포함
        Map<String, Object> out = new HashMap<>();
        out.put("content", content);
        out.put("page", p.getNumber());
        out.put("size", p.getSize());
        out.put("totalElements", p.getTotalElements());
        out.put("totalPages", p.getTotalPages());

        return ResponseEntity.ok(out);
    }

    // 세션 메시지 상세 조회
    // GET /api/admin/chat/sessions/{sessionId}/messages
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<?> sessionMessages(@PathVariable("sessionId") Long sessionId) {
      // 서비스에서 메시지 리스트 조회  
      List<ChatMessage> list = adminChatService.loadMessages(sessionId);
        
      // 관리자 화면에서 대화 로그 보는데 필요한 필드를 뽑아서 map으로 변환
      List<Map<String, Object>> msgs = list.stream().map(m -> {
          Map<String, Object> map = new HashMap<>();
          map.put("chatId", m.getChatId());
          map.put("role", m.getRole());
          map.put("content", m.getContent());
          map.put("createdAt", m.getCreatedAt());
          return map;
      }).collect(Collectors.toList());
        
      // {sessionId, messages} 형태로 반환
      Map<String, Object> out = new HashMap<>();
      out.put("sessionId", sessionId);
      out.put("messages", msgs);

      return ResponseEntity.ok(out);
    }

    // 소프트 삭제
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> softDelete(@PathVariable("sessionId") Long sessionId) {
        adminChatService.softDelete(sessionId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // 복구
    @PatchMapping("/sessions/{sessionId}/restore")
    public ResponseEntity<?> restore(@PathVariable("sessionId") Long sessionId) {
        adminChatService.restore(sessionId);
        return ResponseEntity.ok(Map.of("restored", true));
    }

    // 하드 삭제 (영구 삭제)
    @DeleteMapping("/sessions/{sessionId}/hard")
    public ResponseEntity<?> hardDelete(@PathVariable("sessionId") Long sessionId) {
        adminChatService.hardDelete(sessionId);
        return ResponseEntity.ok(Map.of("hardDeleted", true));
    }
}
