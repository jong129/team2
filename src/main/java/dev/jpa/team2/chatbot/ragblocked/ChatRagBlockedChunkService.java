package dev.jpa.team2.chatbot.ragblocked;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRagBlockedChunkService {

    private final ChatRagBlockedChunkRepository repo;

    // ✅ RAG에서 차단 적용할 때 사용(활성 chunkId만)
    public List<Long> getActiveChunkIds() {
        return repo.findActiveChunkIds();
    }

    // ✅ (관리자) 차단 목록
    public List<ChatRagBlockedChunkDto> listActive() {
        return repo.findByIsActiveOrderByCreatedAtDesc(1).stream()
            .map(ChatRagBlockedChunkDto::from)
            .toList();
    }

    public List<ChatRagBlockedChunkDto> listInactive() {
        return repo.findByIsActiveOrderByCreatedAtDesc(0).stream()
            .map(ChatRagBlockedChunkDto::from)
            .toList();
    }

    // ✅ 단건 차단(upsert)
    @Transactional
    public ChatRagBlockedChunkDto block(Long adminMemberId, Long chunkId, String reason) {
        if (chunkId == null) throw new IllegalArgumentException("chunkId is null");

        var existing = repo.findByChunkId(chunkId).orElse(null);
        if (existing != null) {
            existing.setReason(reason);
            existing.setCreatedBy(adminMemberId);
            existing.setIsActive(1); // activate()
            log.warn("[BlockedChunk] block re-activate | admin={} chunkId={} blockId={}",
                adminMemberId, chunkId, existing.getBlockId());
            return ChatRagBlockedChunkDto.from(existing);
        }

        ChatRagBlockedChunk e = ChatRagBlockedChunk.builder()
            .chunkId(chunkId)
            .reason(reason)
            .createdBy(adminMemberId)
            .isActive(1)
            .build();

        repo.save(e);
        log.warn("[BlockedChunk] block | admin={} chunkId={} blockId={}",
            adminMemberId, chunkId, e.getBlockId());

        return ChatRagBlockedChunkDto.from(e);
    }

    // ✅ 단건 해제
    @Transactional
    public ChatRagBlockedChunkDto unblock(Long adminMemberId, Long chunkId) {
        if (chunkId == null) throw new IllegalArgumentException("chunkId is null");

        ChatRagBlockedChunk e = repo.findByChunkId(chunkId)
            .orElseThrow(() -> new IllegalArgumentException("차단 이력이 없습니다. chunkId=" + chunkId));

        e.setIsActive(0);
        e.setCreatedBy(adminMemberId); // 마지막 조작자 기록(원하면 updatedBy로 분리)
        log.warn("[BlockedChunk] unblock | admin={} chunkId={} blockId={}",
            adminMemberId, chunkId, e.getBlockId());

        return ChatRagBlockedChunkDto.from(e);
    }

    // ✅ 여러 건 자동 차단(upsert)
    @Transactional
    public int blockMany(Long adminMemberId, List<Long> chunkIds, String reason) {
        if (chunkIds == null || chunkIds.isEmpty()) return 0;

        int ok = 0;
        for (Long chunkId : chunkIds) {
            if (chunkId == null) continue;

            var existing = repo.findByChunkId(chunkId).orElse(null);
            if (existing != null) {
                existing.setReason(reason);
                existing.setCreatedBy(adminMemberId);
                existing.setIsActive(1); // activate()
                ok++;
                continue;
            }

            ChatRagBlockedChunk e = ChatRagBlockedChunk.builder()
                .chunkId(chunkId)
                .reason(reason)
                .createdBy(adminMemberId)
                .isActive(1)
                .build();

            repo.save(e);
            ok++;
        }

        log.warn("[BlockedChunk] blockMany | admin={} count={} reason={}", adminMemberId, ok, reason);
        return ok;
    }
}
