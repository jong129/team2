package dev.jpa.team2.checklist.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChecklistTemplateItemRepository
    extends JpaRepository<ChecklistTemplateItem, ChecklistTemplateItemId> {

  @Query("""
    SELECT ti
    FROM ChecklistTemplateItem ti
    WHERE ti.id.templateId = :templateId
      AND ti.activeYn = 'Y'
    ORDER BY ti.itemOrder ASC
  """)
  List<ChecklistTemplateItem> findActiveItemsByTemplateIdOrderByItemOrder(
      @Param("templateId") Long templateId
  );
}
