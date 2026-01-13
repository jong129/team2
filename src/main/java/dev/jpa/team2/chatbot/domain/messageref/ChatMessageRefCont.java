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

    // 회원: 내 AI 답변 근거 조회
    // GET /api/chat/messages/{chatId}/refs
    @GetMapping("/messages/{chatId}/refs")
    public ResponseEntity<ChatMessageRefDto> myRefs(@PathVariable("chatId") Long chatId, HttpSession session) {
        long t0 = System.currentTimeMillis();
        Long memberId = AuthSessionUtil.requireMemberId(session); // 세션에서 memberId 가져오기

        log.info("[ChatMessageRefCont] myRefs | memberId={} chatId={}", memberId, chatId);

        ChatMessageRefDto res = refService.getRefsForMyChat(memberId, chatId);

        log.info("[ChatMessageRefCont] myRefs ok | chatId={} ms={}",
                chatId, System.currentTimeMillis() - t0);
        
        return ResponseEntity.ok(res);  // 응답 : mode = CHAT_REFS, scope = MY 
    }

    // 관리자: 특정 chatId 기준 근거 조회
    // GET /api/chat/admin/messages/{chatId}/refs
    @GetMapping("/admin/messages/{chatId}/refs")
    public ResponseEntity<ChatMessageRefDto> adminRefs(@PathVariable("chatId") Long chatId, HttpSession session) {
        Long adminId = AuthSessionUtil.requireMemberId(session);
        // TODO AuthSessionUtil.requireAdmin(session);

        log.warn("[ChatMessageRefCont] adminRefs | adminId={} chatId={}", adminId, chatId);

        return ResponseEntity.ok(refService.getRefsForAdmin(chatId)); // 응답 : scope = ALL
    }

    // 품질 통계 (전체, 관리자)
    // GET /api/chat/refs/stats/all?days=30&top=10&badN=3
    @GetMapping("/refs/stats/all")
    public ResponseEntity<ChatMessageRefDto> statsAll(
        @RequestParam(name = "days", defaultValue = "30") int days,   // 최근 N일 기준
        @RequestParam(name = "top", defaultValue = "10") int top,      // top개의 chunk 통계
        @RequestParam(name = "badN", defaultValue = "3") int badN,  // 나쁜 답변 기준 dislike threshold
        HttpSession session
    ) {
        Long adminId = AuthSessionUtil.requireMemberId(session);
        // TODO AuthSessionUtil.requireAdmin(session);

        log.info("[ChatMessageRefCont] statsAll | adminId={} days={} top={} badN={}",
                adminId, days, top, badN);

        return ResponseEntity.ok(refService.statsAll(days, top, badN));
    }

    // 나쁜 답변 기반 문제 chunk 후보 (전체, 관리자) : 싫어요 기준 이상 답변만 대상으로 후보뽑는 API
    // GET /api/chat/refs/bad-chunks/all?days=30&top=10&badN=3
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
