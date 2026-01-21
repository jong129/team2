package dev.jpa.team2.checklist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.checklist.model.ChecklistSatisfaction;

public interface ChecklistSatisfactionRepository extends JpaRepository<ChecklistSatisfaction, Long> {

// 기존
  Optional<ChecklistSatisfaction> findBySessionId(Long sessionId);

  boolean existsBySessionId(Long sessionId);

  /*
   * ========================= 관리자 분석용 (JOIN) =========================
   */

  @Query("""
          select count(s)
          from ChecklistSatisfaction s
          join ChecklistSession cs
            on cs.sessionId = s.sessionId
          where cs.templateId = :templateId
            and cs.status = 'COMPLETED'
      """)
  long countByTemplateId(@Param("templateId") Long templateId);

  @Query("""
          select avg(s.rating)
          from ChecklistSatisfaction s
          join ChecklistSession cs
            on cs.sessionId = s.sessionId
          where cs.templateId = :templateId
            and cs.status = 'COMPLETED'
      """)
  Double findAvgRatingByTemplateId(@Param("templateId") Long templateId);

  @Query("""
          select s
          from ChecklistSatisfaction s
          join ChecklistSession cs
            on cs.sessionId = s.sessionId
          where cs.templateId = :templateId
            and cs.status = 'COMPLETED'
          order by s.createdAt desc
      """)
  List<ChecklistSatisfaction> findRecentByTemplateId(@Param("templateId") Long templateId, Pageable pageable);
}
