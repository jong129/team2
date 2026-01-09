package dev.jpa.team2.chatbot.feedback;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/feedback")
public class ChatFeedbackStatsCont {

    private final ChatFeedbackStatsService service;

    // GET /api/chat/feedback/stats?days=30&top=10
    @GetMapping("/stats")
    public ResponseEntity<ChatFeedbackStatsDto> stats(
        @RequestParam(name = "days", defaultValue = "30") int days,
        @RequestParam(name = "top",  defaultValue = "10") int top,
        HttpSession session
    ) {
        Long adminId = AuthSessionUtil.requireMemberId(session);
        return ResponseEntity.ok(service.statsAll(days, top));
    }


}
