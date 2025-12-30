package dev.jpa.team2.checklist.pre;

import dev.jpa.team2.checklist.model.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * PRE 체크리스트 항목 조회 Repository
 * (PRE 기능에서만 사용)
 */
public interface PreChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    /**
     * 템플릿 항목 중 ACTIVE_YN='Y' 만 순서대로 조회
     */
    List<ChecklistItem> findByTemplate_TemplateIdAndActiveYnOrderByItemOrderAsc(
            Long templateId,
            String activeYn
    );
}
