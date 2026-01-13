package dev.jpa.team2.checklist.admin;

import dev.jpa.team2.checklist.model.ItemMasterRowDTO;
import dev.jpa.team2.checklist.model.Phase;
import dev.jpa.team2.checklist.model.TemplateItemRowDTO;
import dev.jpa.team2.checklist.model.TemplateItemUpsertReq;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/checklists")
public class AdminChecklistV2Cont {

  private final AdminChecklistV2Service service;

  // ✅ 항목 풀 목록
  // GET /admin/checklists/item-masters?phase=POST&postGroupCode=POST_A&activeYn=Y&keyword=...
  @GetMapping("/item-masters")
  public Page<ItemMasterRowDTO> listItemMasters(
      @RequestParam(name = "phase", required = false) Phase phase,
      @RequestParam(name = "postGroupCode", required = false) String postGroupCode,
      @RequestParam(name = "activeYn", required = false) String activeYn,
      @RequestParam(name = "keyword", required = false) String keyword,
      @PageableDefault(size = 10, sort = "itemMasterId", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    return service.listItemMasters(phase, postGroupCode, activeYn, keyword, pageable);
  }

  // ✅ 특정 템플릿에 포함된 항목(구성) 조회
  @GetMapping("/templates/{templateId}/items")
  public List<TemplateItemRowDTO> getTemplateItems(@PathVariable("templateId") Long templateId) {
    return service.getTemplateItems(templateId);
  }

  // ✅ 특정 템플릿 구성 전체 저장(교체)
  @PutMapping("/templates/{templateId}/items")
  public void replaceTemplateItems(
      @PathVariable("templateId") Long templateId,
      @RequestBody List<TemplateItemUpsertReq> reqs
  ) {
    service.replaceTemplateItems(templateId, reqs);
  }
  
  @PostMapping("/templates/{templateId}/clone")
  public Map<String, Object> cloneTemplate(@PathVariable("templateId") Long templateId) {
    Long newId = service.cloneTemplate(templateId);
    return Map.of("newTemplateId", newId);
  }

  @PostMapping("/templates/{templateId}/publish")
  public void publishTemplate(@PathVariable("templateId") Long templateId) {
    service.publishTemplate(templateId);
  }

}
