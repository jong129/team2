package dev.jpa.team2.chatbot.message;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import dev.jpa.team2.chatbot.session.ChatSessionDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatMessageCont {

    private final ChatMessageService messageService;

    // 세션 메시지 전체 로딩
    // GET /api/chat/sessions/{sessionId}/messages
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatMessagesResponseDto> messages(
        @PathVariable("sessionId") Long sessionId,
        @RequestParam(name="limit", defaultValue="50") int limit,
        HttpSession httpSession
    ) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);

        // limit은 현재 적용 안 함(Repo를 Pageable로 바꿔서 적용 가능)
        return ResponseEntity.ok(messageService.loadSessionMessages(memberId, sessionId));
    }

    // 키워드 검색 (날짜별 그룹)
    // GET /api/chat/messages/search?keyword=...&size=50
    @GetMapping("/messages/search")
    public ResponseEntity<List<ChatSessionDto.GroupedByDate<ChatSessionDto.SearchResultItem>>> search(
        @RequestParam("keyword") String keyword,
        @RequestParam(name = "size", defaultValue = "50") int size,
        HttpSession httpSession
    ) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);
        return ResponseEntity.ok(messageService.searchMyMessages(memberId, keyword, size));
    }

}
