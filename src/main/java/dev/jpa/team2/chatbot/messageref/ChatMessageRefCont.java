package dev.jpa.team2.chatbot.messageref;

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

    // ==================================================
    // 회원: 내 AI 답변 근거
    // GET /api/chat/messages/{chatId}/refs
    // ==================================================
    @GetMapping("/messages/{chatId}/refs")
    public ResponseEntity<ChatMessageRefDto> myRefs(
        @PathVariable Long chatId,
        HttpSession session
    ) {
        long t0 = System.currentTimeMillis();
        try {
            Long memberId = AuthSessionUtil.requireMemberId(session);

            log.info("[ChatMessageRefCont] myRefs | memberId={} chatId={}",
                    memberId, chatId);

            ChatMessageRefDto res =
                refService.getRefsForMyChat(memberId, chatId);

            log.info("[ChatMessageRefCont] myRefs ok | chatId={} ms={}",
                    chatId, System.currentTimeMillis() - t0);

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("[ChatMessageRefCont] myRefs failed | chatId={}", chatId, e);
            throw e;
        }
    }

    // ==================================================
    // 관리자/디버그
    // GET /api/chat/admin/messages/{chatId}/refs
    // ==================================================
    @GetMapping("/admin/messages/{chatId}/refs")
    public ResponseEntity<ChatMessageRefDto> adminRefs(
        @PathVariable Long chatId,
        HttpSession session
    ) {
        try {
            Long memberId = AuthSessionUtil.requireMemberId(session);

            // TODO 관리자 권한 체크
            // AuthSessionUtil.requireAdmin(session);

            log.warn("[ChatMessageRefCont] adminRefs | adminId={} chatId={}",
                    memberId, chatId);

            return ResponseEntity.ok(refService.getRefsForAdmin(chatId));

        } catch (Exception e) {
            log.error("[ChatMessageRefCont] adminRefs failed | chatId={}", chatId, e);
            throw e;
        }
    }

    // ==================================================
    // 품질 분석: 내 것
    // GET /api/chat/refs/stats/my?days=30&top=10
    // ==================================================
    @GetMapping("/refs/stats/my")
    public ResponseEntity<ChatMessageRefDto> statsMy(
        @RequestParam(defaultValue = "30") int days,
        @RequestParam(defaultValue = "10") int top,
        HttpSession session
    ) {
        try {
            Long memberId = AuthSessionUtil.requireMemberId(session);
            return ResponseEntity.ok(refService.statsMy(memberId, days, top));
        } catch (Exception e) {
            log.error("[ChatMessageRefCont] statsMy failed | days={} top={}",
                    days, top, e);
            throw e;
        }
    }

    // ==================================================
    // 품질 분석: 전체
    // GET /api/chat/refs/stats/all?days=30&top=10
    // ==================================================
    @GetMapping("/refs/stats/all")
    public ResponseEntity<ChatMessageRefDto> statsAll(
        @RequestParam(defaultValue = "30") int days,
        @RequestParam(defaultValue = "10") int top,
        HttpSession session
    ) {
        try {
            Long memberId = AuthSessionUtil.requireMemberId(session);

            // TODO 관리자 권한 체크
            // AuthSessionUtil.requireAdmin(session);

            return ResponseEntity.ok(refService.statsAll(days, top));
        } catch (Exception e) {
            log.error("[ChatMessageRefCont] statsAll failed | days={} top={}",
                    days, top, e);
            throw e;
        }
    }
}
