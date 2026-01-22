package dev.jpa.team2.checklist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.checklist.model.ChecklistItem;

public interface ItemRepository
        extends JpaRepository<ChecklistItem, Long> {

    // =========================
    // 체크리스트 화면 렌더링
    // =========================
    List<ChecklistItem> findBySessionId(Long sessionId);

    List<ChecklistItem>
        findBySessionIdOrderByItemOrderAsc(Long sessionId);

    // =========================
    // POST 이어하기 방어 로직
    // =========================
    boolean existsBySessionId(Long sessionId);

    // =========================
    // PRE reset 시 전체 삭제
    // =========================
    void deleteBySessionId(Long sessionId);

    // =========================
    // PRE → POST 분기 판단용 (기존)
    // =========================
    List<ChecklistItem> findBySessionIdAndItemIdIn(
        Long sessionId,
        List<Long> itemIds
    );

    // =========================================================
    // 🔥 관리자 / 분기 판단 공통용
    // - PRE 세션에서 NOT_DONE 상태인 항목만 조회
    // - AI 중요도 점수 계산 대상
    //
    // ❗ [확장 포인트]
    //   PostChecklistSessionService.resolvePostGroupCode()
    //   에서도 동일 메서드 사용 가능
    // =========================================================
    @Query("""
        select i
        from ChecklistItem i
        join ChecklistResponse r
          on r.itemId = i.itemId
         and r.sessionId = :sessionId
        where r.checkStatus = 'NOT_DONE'
        order by i.itemOrder asc
    """)
    List<ChecklistItem> findNotDoneItemsByPreSessionId(
        @Param("sessionId") Long sessionId
    );
}
