package dev.jpa.team2.checklist.admin;

import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.Phase;
import dev.jpa.team2.checklist.model.TemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {

  // POST: 같은 그룹(phase+postGroupCode)에서 최신 버전
  Optional<ChecklistTemplate> findFirstByPhaseAndPostGroupCodeOrderByVersionNoDesc(
      Phase phase, String postGroupCode
  );

  // POST: 같은 그룹에서 ACTIVE 1개 찾기
  Optional<ChecklistTemplate> findFirstByPhaseAndPostGroupCodeAndStatusOrderByVersionNoDesc(
      Phase phase, String postGroupCode, TemplateStatus status
  );

  // PRE: postGroupCode null
  Optional<ChecklistTemplate> findFirstByPhaseAndPostGroupCodeIsNullOrderByVersionNoDesc(Phase phase);

  Optional<ChecklistTemplate> findFirstByPhaseAndPostGroupCodeIsNullAndStatusOrderByVersionNoDesc(
      Phase phase, TemplateStatus status
  );
}
