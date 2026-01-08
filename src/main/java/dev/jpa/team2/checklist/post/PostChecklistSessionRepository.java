package dev.jpa.team2.checklist.post;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.model.Phase;

public interface PostChecklistSessionRepository extends JpaRepository<ChecklistSession, Long> {

  Optional<ChecklistSession> findTopByMemberIdAndPhaseAndDeletedYnOrderByStartedAtDesc(Long memberId, Phase phase,
      String deletedYn);

  // ✅ (추가) 진행중 POST 세션 재사용
  Optional<ChecklistSession> findTopByMemberIdAndPhaseAndStatusAndDeletedYnOrderByStartedAtDesc(
      Long memberId, Phase phase, String status, String deletedYn
  );
  
  Optional<ChecklistSession> findBySessionIdAndDeletedYn(Long sessionId, String deletedYn);

  Page<ChecklistSession> findByMemberIdAndPhaseAndDeletedYn(Long memberId, Phase phase, String deletedYn,
      Pageable pageable);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
        UPDATE ChecklistSession s
           SET s.status = 'COMPLETED',
               s.completedAt = :now
         WHERE s.sessionId = :sessionId
           AND s.deletedYn = 'N'
      """)
  int markCompleted(@Param("sessionId") Long sessionId, @Param("now") LocalDateTime now);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
        UPDATE ChecklistSession s
           SET s.deletedYn = 'Y',
               s.deletedAt = :now
         WHERE s.sessionId = :sessionId
           AND s.phase = dev.jpa.team2.checklist.model.Phase.POST
           AND s.deletedYn = 'N'
      """)
  int softDeletePostSession(@Param("sessionId") Long sessionId, @Param("now") LocalDateTime now);
}
