package dev.jpa.team2.checklist.pre;

import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.Phase;
import dev.jpa.team2.checklist.model.TemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * PRE 체크리스트 템플릿 조회 Repository
 * (PRE 기능에서만 사용)
 */
public interface PreChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {

    /**
     * PRE + ACTIVE 템플릿 중 최신 버전 1개 조회
     */
    Optional<ChecklistTemplate> findFirstByPhaseAndStatusOrderByVersionNoDesc(
            Phase phase,
            TemplateStatus status
    );
}
