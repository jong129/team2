package dev.jpa.team2.checklist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.model.ChecklistItem;

public interface ItemRepository
        extends JpaRepository<ChecklistItem, Long> {

    // 체크리스트 화면 렌더링
    List<ChecklistItem> findBySessionId(Long sessionId);

    List<ChecklistItem>
    findBySessionIdOrderByItemOrderAsc(Long sessionId);
    
    // ✅ POST 이어하기 방어 로직용 (세션에 아이템 존재 여부)
    boolean existsBySessionId(Long sessionId);
    
    // PRE reset 시 전체 삭제
    void deleteBySessionId(Long sessionId);
    
    List<ChecklistItem> findBySessionIdAndItemIdIn(
        Long sessionId,
        List<Long> itemIds
    );

}
