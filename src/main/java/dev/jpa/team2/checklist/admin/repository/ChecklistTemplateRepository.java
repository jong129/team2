package dev.jpa.team2.checklist.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.model.ChecklistTemplate;

/**
 * 체크리스트 템플릿 기본 Repository
 */
public interface ChecklistTemplateRepository
        extends JpaRepository<ChecklistTemplate, Long>,
                ChecklistTemplateRepositoryCustom {
}
