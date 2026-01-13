package dev.jpa.team2.chatbot.domain.ragblocked;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 차단 등록/해제를 DB 유니크 + 상태 플래그 기반으로 안정적으로 처리하는 핵심 로직

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRagBlockedChunkService {

    private final ChatRagBlockedChunkRepository repo;

    // RAG에서 사용할 활성 차단 chunkId 리스트
    public List<Long> getActiveChunkIds() {
        return repo.findActiveChunkIds();
    }

    // 관리자 차단 목록
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

    // 단건 차단 (upsert) : 이미 있으면 업데이트+활성화 / 없으면 새로 생성
    @Transactional
    public ChatRagBlockedChunkDto block(Long adminMemberId, Long chunkId, String reason) {
        if (chunkId == null) throw new IllegalArgumentException("chunkId is null"); // chunkId null이면 예외

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

    // 단건 해제 : 삭제가 아니라 비활성이라 언제 차단했다가 해제했는지 이력 관리 가능
    @Transactional
    public ChatRagBlockedChunkDto unblock(Long adminMemberId, Long chunkId) {
        if (chunkId == null) throw new IllegalArgumentException("chunkId is null");

        ChatRagBlockedChunk e = repo.findByChunkId(chunkId) 
        // findByChunkId 없으면 
            .orElseThrow(() -> new IllegalArgumentException("차단 이력이 없습니다. chunkId=" + chunkId)); 
        // 있으면
        e.setIsActive(0);
        e.setCreatedBy(adminMemberId); // 마지막 조작자 기록
        log.warn("[BlockedChunk] unblock | admin={} chunkId={} blockId={}",
            adminMemberId, chunkId, e.getBlockId());

        return ChatRagBlockedChunkDto.from(e);
    }

    // 여러 건 자동 차단 (upsert) : auto-block에서 후보 chunk 리스트를 받아서 한 번에 반영하는 용도
    @Transactional
    public int blockMany(Long adminMemberId, List<Long> chunkIds, String reason) {
        if (chunkIds == null || chunkIds.isEmpty()) return 0; // chunkIds 비었으면 0

        int ok = 0;
        // 각 chunkId에 대해 기존 있으면 reason/createdBy/isActive=1 갱신 / 없으면 생성 후 save
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
