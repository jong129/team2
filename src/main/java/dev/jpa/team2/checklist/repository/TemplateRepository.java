package dev.jpa.team2.checklist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.TemplateStatus;
import dev.jpa.team2.checklist.model.ChecklistTemplate;

public interface TemplateRepository extends JpaRepository<ChecklistTemplate, Long> {

  // ✅ PRE (상태 무관, 1개 고정)
  Optional<ChecklistTemplate> findFirstByPhaseOrderByVersionNoDesc(
      ChecklistPhase phase
  );

  // POST (그대로 유지)
  Optional<ChecklistTemplate> findFirstByPhaseAndStatusAndPostGroupCode(
      ChecklistPhase phase,
      TemplateStatus status,
      String postGroupCode
  );
}
