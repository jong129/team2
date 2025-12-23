package dev.jpa.team2.chatbot;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    // 🔥 핵심
    @PostMapping("/ask")
    public RagDto ask(@RequestBody RagDto dto) {
        return ragService.ask(dto);
    }

    // ✅ 헬스체크 (브라우저용)
    @GetMapping("/ping")
    public String ping() {
        return "RAG API OK";
    }

    // ✅ 세션 대화 조회
    @GetMapping("/history/{sessionId}")
    public List<ChatHistory> history(@PathVariable Long sessionId) {
        return ragService.getHistory(sessionId);
    }

}

