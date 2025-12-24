package dev.jpa.team2.chatbot;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatHistoryCont {

    private final ChatHistoryService messageService;

    // ✅ 미니팝업/페이지 공용: 세션 메시지 전체 로딩
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatMessagesResponseDto> messages(
        @PathVariable("sessionId") Long sessionId,
        @RequestParam(name="limit", defaultValue="50") int limit,
        HttpSession httpSession
    ) {
        Long memberId = AuthSessionUtil.requireMemberId(httpSession);
        // limit 쓰든 안 쓰든 일단 파라미터 바인딩은 이렇게
        return ResponseEntity.ok(messageService.loadSessionMessages(memberId, sessionId));
    }


    // ✅ aibotpage: 키워드 검색
    @GetMapping("/messages/search")
    public ResponseEntity<List<GroupedSearchResultsDto>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "50") int size,
            HttpSession httpSession) {

        Long memberId = AuthSessionUtil.requireMemberId(httpSession);
        return ResponseEntity.ok(messageService.searchMyMessages(memberId, keyword, size));
    }
}
