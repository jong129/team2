package dev.jpa.team2.chatbot.domain.messageref;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatMessageRefCont {

    private final ChatMessageRefService refService;

    // ==========================================================
    // 1) 회원: 내 AI 답변 근거 조회
    // GET /api/chat/messages/{chatId}/refs
    // ==========================================================
    @GetMapping("/messages/{chatId}/refs")
    public ResponseEntity<ChatMessageRefDto> myRefs(
        @PathVariable("chatId") Long chatId,
        HttpSession session
    ) {
        long t0 = System.currentTimeMillis();
        Long memberId = AuthSessionUtil.requireMemberId(session);

        log.info("[ChatMessageRefCont] myRefs | memberId={} chatId={}", memberId, chatId);

        ChatMessageRefDto res = refService.getRefsForMyChat(memberId, chatId);

        log.info("[ChatMessageRefCont] myRefs ok | chatId={} ms={}",
                chatId, System.currentTimeMillis() - t0);

        return ResponseEntity.ok(res);
    }

    // ==========================================================
    // 2) 관리자/디버그: chatId 기준 근거 조회
    // GET /api/chat/admin/messages/{chatId}/refs
    // ==========================================================
    @GetMapping("/admin/messages/{chatId}/refs")
    public ResponseEntity<ChatMessageRefDto> adminRefs(
        @PathVariable("chatId") Long chatId,
        HttpSession session
    ) {
        Long adminId = AuthSessionUtil.requireMemberId(session);
        // TODO AuthSessionUtil.requireAdmin(session);

        log.warn("[ChatMessageRefCont] adminRefs | adminId={} chatId={}", adminId, chatId);

        return ResponseEntity.ok(refService.getRefsForAdmin(chatId));
    }

    // ==========================================================
    // 3) 품질 분석: 전체(관리자)
    // GET /api/chat/refs/stats/all?days=30&top=10&badN=3
    // ==========================================================
    @GetMapping("/refs/stats/all")
    public ResponseEntity<ChatMessageRefDto> statsAll(
        @RequestParam(name = "days", defaultValue = "30") int days,
        @RequestParam(name = "top", defaultValue = "10") int top,
        @RequestParam(name = "badN", defaultValue = "3") int badN,
        HttpSession session
    ) {
        Long adminId = AuthSessionUtil.requireMemberId(session);
        // TODO AuthSessionUtil.requireAdmin(session);

        log.info("[ChatMessageRefCont] statsAll | adminId={} days={} top={} badN={}",
                adminId, days, top, badN);

        return ResponseEntity.ok(refService.statsAll(days, top, badN));
    }

    // ==========================================================
    // 4) ✅ 나쁜 답변 기반 문제 chunk 후보(전체/관리자)
    // GET /api/chat/refs/bad-chunks/all?days=30&top=10&badN=3
    // ==========================================================
    @GetMapping("/refs/bad-chunks/all")
    public ResponseEntity<ChatMessageRefDto> badChunksAll(
        @RequestParam(name = "days", defaultValue = "30") int days,
        @RequestParam(name = "top", defaultValue = "10") int top,
        @RequestParam(name = "badN", defaultValue = "3") int badN,
        HttpSession session
    ) {
        Long adminId = AuthSessionUtil.requireMemberId(session);
        // TODO AuthSessionUtil.requireAdmin(session);

        log.info("[ChatMessageRefCont] badChunksAll | adminId={} days={} top={} badN={}",
                adminId, days, top, badN);

        return ResponseEntity.ok(refService.badChunksAll(days, top, badN));
    }
}
