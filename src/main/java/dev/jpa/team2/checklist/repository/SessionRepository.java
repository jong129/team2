package dev.jpa.team2.checklist.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.SessionStatus;
import dev.jpa.team2.checklist.enums.Yn;
import dev.jpa.team2.checklist.model.ChecklistSession;

public interface SessionRepository
        extends JpaRepository<ChecklistSession, Long> {

    List<ChecklistSession>
        findByMemberIdAndPhaseAndStatus(
            Long memberId,
            ChecklistPhase phase,
            SessionStatus status
        );
    
    // 이어하기용
    Optional<ChecklistSession>
    findTopByMemberIdAndPhaseAndStatusOrderBySessionIdDesc(
        Long memberId,
        ChecklistPhase phase,
        SessionStatus status
    );
    
    @Query("""
        select s
        from ChecklistSession s
        where s.memberId = :memberId
          and s.phase = :phase
          and (:status is null or s.status = :status)
          and (:from is null or s.startedAt >= :from)
          and (
                :to is null
                or s.completedAt is null
                or s.completedAt <= :to
              )
          and s.deletedYn = dev.jpa.team2.checklist.enums.Yn.N
    """)
    Page<ChecklistSession> searchPreHistory(
        @Param("memberId") Long memberId,
        @Param("phase") ChecklistPhase phase,
        @Param("status") SessionStatus status,
        @Param("from") Date from,
        @Param("to") Date to,
        Pageable pageable
    );

 // 특정 세션을 memberId 기준으로 조회 (소유자 검증용)
    Optional<ChecklistSession> findBySessionIdAndMemberId(Long sessionId, Long memberId);

    /**
     * ✅ 같은 PRE 세션에서 시작된 POST 세션 이어하기용
     */
    Optional<ChecklistSession>
    findByPreSessionIdAndPhaseAndStatus(
        Long preSessionId,
        ChecklistPhase phase,
        SessionStatus status
    );
    
    List<ChecklistSession>
    findTop20ByPhaseAndStatusAndDeletedYnOrderByCompletedAtDesc(
        ChecklistPhase phase,
        SessionStatus status,
        Yn deletedYn
    );



}
