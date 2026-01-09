package dev.jpa.team2.chatbot.ragblocked;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import dev.jpa.team2.chatbot.messageref.ChatMessageRefDto;
import dev.jpa.team2.chatbot.messageref.ChatMessageRefService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rag/blocked-chunks")
public class ChatRagBlockedChunkCont {

    private final ChatRagBlockedChunkService service;

    // ✅ 추가: bad-chunks 후보를 가져오기 위한 서비스
    private final ChatMessageRefService refService;

    // (관리자) 차단 목록 조회
    @GetMapping
    public ResponseEntity<List<ChatRagBlockedChunkDto>> list(
        @RequestParam(name = "active", defaultValue = "true") boolean active,
        HttpSession session
    ) {
        Long adminId = AuthSessionUtil.requireMemberId(session);
        // TODO AuthSessionUtil.requireAdmin(session);

        return ResponseEntity.ok(active ? service.listActive() : service.listInactive());
    }

    // (관리자) chunk 차단
    @PostMapping
    public ResponseEntity<ChatRagBlockedChunkDto> block(
        @RequestBody ChatRagBlockedChunkDto req,
        HttpSession session
    ) {
        Long adminId = AuthSessionUtil.requireMemberId(session);
        // TODO AuthSessionUtil.requireAdmin(session);

        return ResponseEntity.ok(service.block(adminId, req.getChunkId(), req.getReason()));
    }

    // (관리자) chunk 차단 해제
    @PostMapping("/unblock/{chunkId}")
    public ResponseEntity<ChatRagBlockedChunkDto> unblock(
        @PathVariable("chunkId") Long chunkId,
        HttpSession session
    ) {
        Long adminId = AuthSessionUtil.requireMemberId(session);
        // TODO AuthSessionUtil.requireAdmin(session);

        return ResponseEntity.ok(service.unblock(adminId, chunkId));
    }

    // ==========================================================
    // ✅ 자동 차단 기능 (bad-chunks 후보 -> 차단 테이블에 일괄 등록)
    // ==========================================================

    /**
     * (관리자) 전체 기준: 싫어요 많이 달린 답변들에서 자주 등장하는 chunk를 자동 차단
     * POST /api/chat/rag/blocked-chunks/auto-block/all?days=30&top=20&badN=3
     */
    @PostMapping("/auto-block/all")
    public ResponseEntity<AutoBlockRes> autoBlockAll(
        @RequestParam(name = "days", defaultValue = "30") int days,
        @RequestParam(name = "top", defaultValue = "20") int top,
        @RequestParam(name = "badN", defaultValue = "3") int badN,
        @RequestParam(name = "reason", required = false) String reason,
        HttpSession session
    ) {
        Long adminId = AuthSessionUtil.requireMemberId(session);
        // TODO AuthSessionUtil.requireAdmin(session);

        ChatMessageRefDto dto = refService.badChunksAll(days, top, badN);

        List<Long> chunkIds = (dto.getBadChunks() == null) ? List.of()
            : dto.getBadChunks().stream()
                .map(ChatMessageRefDto.TopChunkStat::getChunkId)
                .toList();

        String finalReason = (reason == null || reason.isBlank())
            ? ("auto-block(all): bad-chunks days=" + days + ", badN=" + badN)
            : reason;

        int blockedCount = service.blockMany(adminId, chunkIds, finalReason);

        log.warn("[BlockedChunkCont] autoBlockAll | adminId={} days={} top={} badN={} candidates={} blocked={}",
            adminId, days, top, badN, chunkIds.size(), blockedCount);

        return ResponseEntity.ok(new AutoBlockRes(blockedCount, chunkIds.size(), finalReason));
    }

    /**
     * (관리자/운영자) 내 데이터 기준(회원별): 내 세션/질문에서 bad-chunk 후보를 뽑아 자동 차단
     * POST /api/chat/rag/blocked-chunks/auto-block/my?days=30&top=20&badN=3
     */
    @PostMapping("/auto-block/my")
    public ResponseEntity<AutoBlockRes> autoBlockMy(
        @RequestParam(name = "days", defaultValue = "30") int days,
        @RequestParam(name = "top", defaultValue = "20") int top,
        @RequestParam(name = "badN", defaultValue = "3") int badN,
        @RequestParam(name = "reason", required = false) String reason,
        HttpSession session
    ) {
        Long adminId = AuthSessionUtil.requireMemberId(session);
        // TODO AuthSessionUtil.requireAdmin(session);

        ChatMessageRefDto dto = refService.badChunksMy(adminId, days, top, badN);

        List<Long> chunkIds = (dto.getBadChunks() == null) ? List.of()
            : dto.getBadChunks().stream()
                .map(ChatMessageRefDto.TopChunkStat::getChunkId)
                .toList();

        String finalReason = (reason == null || reason.isBlank())
            ? ("auto-block(my): bad-chunks days=" + days + ", badN=" + badN)
            : reason;

        int blockedCount = service.blockMany(adminId, chunkIds, finalReason);

        log.warn("[BlockedChunkCont] autoBlockMy | adminId={} days={} top={} badN={} candidates={} blocked={}",
            adminId, days, top, badN, chunkIds.size(), blockedCount);

        return ResponseEntity.ok(new AutoBlockRes(blockedCount, chunkIds.size(), finalReason));
    }

    // 응답 DTO
    @Getter
    @AllArgsConstructor
    public static class AutoBlockRes {
        private int blockedCount;     // 실제 처리된 수
        private int candidateCount;   // 후보 수
        private String reason;
    }
}
