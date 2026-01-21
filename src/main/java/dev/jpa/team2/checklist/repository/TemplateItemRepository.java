package dev.jpa.team2.checklist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import dev.jpa.team2.checklist.model.ChecklistTemplateItemId;

public interface TemplateItemRepository
        extends JpaRepository<ChecklistTemplateItem, ChecklistTemplateItemId> {

    /**
     * ✅ Template → Session 아이템 조회
     */
    List<ChecklistTemplateItem>
        findByTemplate_TemplateIdOrderByItemOrderAsc(Long templateId);

    /**
     * ✅ Summary 계산용
     */
    Optional<ChecklistTemplateItem>
        findByTemplate_TemplateIdAndItemMaster_ItemMasterId(
            Long templateId,
            Long itemMasterId
        );
}
