package dev.jpa.team2.chatbot.domain.rag;

import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class ChatRagCont {

    private final ChatRagService ragService;

    @PostMapping("/ask")
    public ChatRagDto ask(@RequestBody ChatRagDto dto, HttpSession session) {
        Long memberId = AuthSessionUtil.requireMemberId(session);
        return ragService.ask(dto, memberId); // 서비스로 위임
    }

    @GetMapping("/ping")
    public String ping() {
        return "RAG API OK";
    }
}
