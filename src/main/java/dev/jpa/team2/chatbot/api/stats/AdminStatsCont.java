package dev.jpa.team2.chatbot.api.stats;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

// 관리자 전용 통계 API : 챗봇의 사용량/성능 통계(usage)와 품질 통계(feedback)를 각각의 서비스에 위임해 반화하는 컨트롤러

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/stats") 
public class AdminStatsCont {

    private final ChatUsageStatsService usageStatsService;
    private final ChatFeedbackStatsService feedbackStatsService;
    
    // 최근 N일간 챗봇의 사용량과 성능(토큰, 지연시간)을 집계해서 반환
    // GET /api/admin/stats/usage?days=30
    @GetMapping("/usage") 
    public ResponseEntity<ChatUsageStatsDto> usage(
        @RequestParam(name = "days", defaultValue = "30") int days,
        HttpSession session
    ) {
        AuthSessionUtil.requireMemberId(session); // 나중에 requireAdmin으로 교체, 현재는 로그인 여부만 확인
        int safeDays = Math.max(1, Math.min(days, 365));  // 컨트롤러에서 비정상 입력 차단 (0일, 음수, 5년치 통계 등 모두 차단)
        return ResponseEntity.ok(usageStatsService.loadUsageStats(safeDays)); // 오직 결과 DTO를 그대로 반환, 통계가 어떻게 계산되는지 전혀 모름
    }
    
    // 최근 N일간 챗봇 응답에 대한 좋아요/싫어요 기반 품질 통계를 반환
    // GET /api/admin/stats/feedback?days=30&top=10
    @GetMapping("/feedback")
    public ResponseEntity<ChatFeedbackStatsDto> feedback(
        @RequestParam(name = "days", defaultValue = "30") int days,
        @RequestParam(name = "top",  defaultValue = "10") int top,
        HttpSession session
    ) {
        AuthSessionUtil.requireMemberId(session); // 나중에 requireAdmin으로 교체
        return ResponseEntity.ok(feedbackStatsService.statsAll(days, top));
    }
}
