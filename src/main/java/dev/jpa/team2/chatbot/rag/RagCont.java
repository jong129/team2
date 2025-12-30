package dev.jpa.team2.chatbot.rag;

import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import dev.jpa.team2.chatbot.message.ChatMessagesResponseDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagCont {

    private final RagService ragService;

    // 질문 -> RAG 답변 생성 + (USER/ASSISTANT 메시지 저장 + refs 저장)
    @PostMapping("/ask")
    public RagDto ask(@RequestBody RagDto dto, HttpSession session) {
        Long memberId = AuthSessionUtil.requireMemberId(session);
        return ragService.ask(dto, memberId);
    }

    // 헬스체크
    @GetMapping("/ping")
    public String ping() {
        return "RAG API OK";
    }

    // (옵션) 세션 대화 조회: 디버그/테스트용
    // aibotpage 쪽은 보통 /api/chat/sessions/{id}/messages 를 쓰는 게 더 정석
    @GetMapping("/history/{sessionId}")
    public ChatMessagesResponseDto history(@PathVariable Long sessionId, HttpSession session) {
        Long memberId = AuthSessionUtil.requireMemberId(session);
        return ragService.getHistory(memberId, sessionId);
    }
}
