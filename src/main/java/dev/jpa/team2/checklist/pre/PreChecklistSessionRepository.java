package dev.jpa.team2.checklist.pre;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.model.Phase;

/**
 * CHECKLIST_SESSION 조회/저장 Repository
 * - PRE 진행중 세션이 있으면 재사용하기 위해 사용
 * - 기록보기(히스토리) 목록 조회에도 사용
 */
public interface PreChecklistSessionRepository extends JpaRepository<ChecklistSession, Long>, JpaSpecificationExecutor<ChecklistSession> {

    /**
     * (기존) 사용자(memberId)의 특정 단계(PRE/POST) + 상태(IN_PROGRESS/COMPLETED) 세션 1개 조회
     * ⚠️ 소프트삭제가 도입되면 이 메서드는 "삭제된 세션"까지 잡힐 수 있음.
     *   가능하면 아래 deletedYn 포함 메서드로 교체 추천.
     */
    Optional<ChecklistSession> findFirstByMemberIdAndPhaseAndStatus(
            Long memberId,
            Phase phase,
            String status
    );

    /**
     * ✅ (추천) 이어하기용: 삭제되지 않은 진행중 세션 1개 조회
     */
    Optional<ChecklistSession> findFirstByMemberIdAndPhaseAndStatusAndDeletedYn(
            Long memberId,
            Phase phase,
            String status,
            String deletedYn // "N"
    );

    /**
     * ✅ 기록보기용: 삭제되지 않은 세션 목록(최신순)
     */
    List<ChecklistSession> findByMemberIdAndPhaseAndDeletedYnOrderByStartedAtDesc(
            Long memberId,
            Phase phase,
            String deletedYn // "N"
    );
    
    
}
