package dev.jpa.team2.checklist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.checklist.enums.CheckStatus;
import dev.jpa.team2.checklist.model.ChecklistResponse;

public interface ResponseRepository
        extends JpaRepository<ChecklistResponse, Long> {

    // 메인 페이지: 진행 여부 판단 (statuses)
    List<ChecklistResponse> findBySessionId(Long sessionId);

    Optional<ChecklistResponse>
    findBySessionIdAndItemId(Long sessionId, Long itemId);
    
    // reset 시 응답 전체 삭제
    void deleteBySessionId(Long sessionId);
    
    @Query("""
        select count(r) > 0
        from ChecklistResponse r
        join ChecklistItem i on r.itemId = i.itemId
        where r.sessionId = :sessionId
          and i.requiredYn = 'Y'
          and r.checkStatus <> dev.jpa.team2.checklist.enums.CheckStatus.DONE
    """)
    boolean existsRequiredNotDone(
        @Param("sessionId") Long sessionId
    );
    
    List<ChecklistResponse> findBySessionIdAndCheckStatus(
        Long sessionId,
        CheckStatus checkStatus
    );


}
