package dev.jpa.team2.checklist.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
  void deleteByTemplate_TemplateId(Long templateId);
  
//baseTemplateId의 CHECKLIST_ITEM 목록(런타임) 가져오기
 List<ChecklistItem> findByTemplate_TemplateIdOrderByItemOrderAsc(Long templateId);
}
