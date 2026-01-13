package dev.jpa.team2.checklist.admin;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import dev.jpa.team2.checklist.ai.AdminPostChecklistAiImproveService;
import dev.jpa.team2.checklist.ai.PostTemplateImproveResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/ai/post/templates")
public class AdminPostChecklistAiImproveController {

  private final AdminPostChecklistAiImproveService service;

  @PostMapping("/{templateId}/improve")
  public PostTemplateImproveResponse improve(
      @PathVariable("templateId") Long templateId
  ) {
      var r = service.createDraftFromBaseTemplate(templateId);
      return PostTemplateImproveResponse.builder()
          .baseTemplateId(r.baseTemplateId())
          .newTemplateId(r.newTemplateId())
          .newVersionNo(r.newVersionNo())
          .message("AI 개선 초안 템플릿이 생성되었습니다.")
          .build();
  }

}
