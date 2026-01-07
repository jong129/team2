package dev.jpa.team2.checklist.post;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.Phase;
import dev.jpa.team2.checklist.model.TemplateStatus;

public interface PostChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {
    Optional<ChecklistTemplate> findTopByPhaseAndPostGroupCodeAndStatusOrderByVersionNoDesc(
        Phase phase, String postGroupCode, TemplateStatus status
    );
}
