package dev.jpa.team2.checklist.admin;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.checklist.ai.AiPostChecklistService;
import dev.jpa.team2.checklist.ai.AiPostItemSignalDTO;
import dev.jpa.team2.checklist.ai.AiPostItemStatDTO;
import dev.jpa.team2.checklist.ai.AiPostTemplateRowDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/ai/post")
public class AdminAiPostChecklistController {

  private final AiPostChecklistService service;

  // ✅ 활성 POST 템플릿(POST_A~D) 요약 목록
  @GetMapping("/templates")
  public List<AiPostTemplateRowDTO> listActivePostTemplates() {
    return service.listActivePostTemplates();
  }

  // ✅ 템플릿 1개 항목별 통계
  @GetMapping("/templates/{templateId}/items/stats")
  public List<AiPostItemStatDTO> itemStats(@PathVariable("templateId") Long templateId) {
    return service.getPostTemplateItemStats(templateId);
  }
  
  @GetMapping("/templates/{templateId}/items/signals")
  public List<AiPostItemSignalDTO> itemSignals(@PathVariable("templateId") Long templateId) {
    return service.getItemSignals(templateId);
  }


}
