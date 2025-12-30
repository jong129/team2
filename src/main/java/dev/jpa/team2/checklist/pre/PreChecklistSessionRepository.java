package dev.jpa.team2.checklist.pre;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.model.Phase;

/**
 * CHECKLIST_SESSION 조회/저장 Repository
 * - PRE 진행중 세션이 있으면 재사용하기 위해 사용
 */
public interface PreChecklistSessionRepository extends JpaRepository<ChecklistSession, Long> {

    /**
     * 사용자(memberId)의 특정 단계(PRE/POST) + 상태(IN_PROGRESS/COMPLETED) 세션 1개 조회
     * - 통합 PRE에서는 "PRE + IN_PROGRESS" 한 개만 유지하는 방식으로 사용
     */
    Optional<ChecklistSession> findFirstByMemberIdAndPhaseAndStatus(
            Long memberId,
            Phase phase,
            String status
    );
}
