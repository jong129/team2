package dev.jpa.team2.checklist.admin;

import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/checklists")
public class AdminChecklistCont {

  private final AdminChecklistService service;

  // ✅ 관리자 템플릿 목록 조회
  // 예) GET /admin/checklists/templates?phase=PRE&status=ACTIVE&keyword=등기&page=0&size=10
  @GetMapping("/templates")
  public Page<AdminTemplateRow> listTemplates(
      @RequestParam(value = "phase", required = false) String phase,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "sortKey", required = false, defaultValue = "updatedAt") String sortKey,
      @RequestParam(value = "sortDir", required = false, defaultValue = "desc") String sortDir,
      Pageable pageable
  ) {
    return service.listTemplates(phase, status, keyword, sortKey, sortDir, pageable);
  }
  
  @PatchMapping("/templates/{templateId}/status")
  public void updateStatus(
      @PathVariable("templateId") Long templateId,
      @RequestBody TemplateStatusUpdateReq req
  ) {
    service.updateTemplateStatus(templateId, req.getStatus());
  }

  @GetMapping("/templates/{templateId}")
  public AdminTemplateDetailDTO getTemplate(@PathVariable("templateId") Long templateId) {
    return service.getTemplate(templateId);
  }

  @PatchMapping("/templates/{templateId}/meta")
  public void updateTemplateMeta(
      @PathVariable("templateId") Long templateId,
      @RequestBody AdminTemplateMetaUpdateReq req
  ) {
    service.updateTemplateMeta(templateId, req);
  }

  @PostMapping("/templates")
  public AdminTemplateCreateRes createTemplate(@RequestBody AdminTemplateCreateReq req) {
    Long id = service.createTemplate(req);
    return new AdminTemplateCreateRes(id);
  }



  
}
