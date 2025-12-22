package dev.jpa.team2.checklist.template.repository;

import dev.jpa.team2.checklist.template.entity.ChecklistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChecklistTemplateRepository
        extends JpaRepository<ChecklistTemplate, Long> {

    /**
     * PRE/POST 타입별 활성 템플릿 조회
     * (버전이 여러 개면 최신 버전 사용)
     */
    Optional<ChecklistTemplate>
    findTopByTemplateTypeAndIsActiveYnOrderByVersionNoDesc(
            String templateType,
            String isActiveYn
    );
}
