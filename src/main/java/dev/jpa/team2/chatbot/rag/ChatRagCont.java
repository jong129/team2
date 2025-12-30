package dev.jpa.team2.chatbot.rag;

import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class ChatRagCont {

    private final ChatRagService ragService;

    // 질문 -> RAG 답변 생성 + (USER/AI 메시지 저장 + refs 저장)
    @PostMapping("/ask")
    public ChatRagDto ask(@RequestBody ChatRagDto dto, HttpSession session) {
        Long memberId = AuthSessionUtil.requireMemberId(session);
        return ragService.ask(dto, memberId);
    }

    // ping 체크
    @GetMapping("/ping")
    public String ping() {
        return "RAG API OK";
    }

    // (옵션) 세션 대화 조회: 디버그/테스트용
    // aibotpage 쪽은 보통 /api/chat/sessions/{id}/messages 를 쓰는 게 더 정석
//    @GetMapping("/history/{sessionId}")
//    public ChatMessagesResponseDto history(@PathVariable Long sessionId, HttpSession session) {
//        Long memberId = AuthSessionUtil.requireMemberId(session);
//        return ragService.getHistory(memberId, sessionId);
//    }
}
