package dev.jpa.team2.checklist.admin;

import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import dev.jpa.team2.checklist.model.ChecklistTemplateItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminTemplateItemRepository extends JpaRepository<ChecklistTemplateItem, ChecklistTemplateItemId> {

  List<ChecklistTemplateItem> findByTemplate_TemplateIdOrderByItemOrderAsc(Long templateId);

  void deleteByTemplate_TemplateId(Long templateId);
  
  long countByTemplate_TemplateId(Long templateId);

  long countByTemplate_TemplateIdAndActiveYn(Long templateId, String activeYn);

  
}
