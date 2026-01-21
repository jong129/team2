package dev.jpa.team2.checklist.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.admin.dto.ChecklistSummaryAiResponseDto;
import dev.jpa.team2.checklist.admin.service.AdminPostChecklistSummaryService;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 POST 체크리스트 만족도 요약 Controller
 */
@RestController
@RequestMapping("/admin/checklists/post/templates")
@RequiredArgsConstructor
public class AdminPostChecklistSummaryController {

    private final AdminPostChecklistSummaryService summaryService;

    /**
     * 사용자 만족도 요약 (LLM)
     *
     * POST /admin/checklists/post/templates/{templateId}/summary
     */
    @PostMapping("/{templateId}/summary")
    public ResponseEntity<ChecklistSummaryAiResponseDto> summary(
        @PathVariable(name = "templateId") Long templateId
    ) {
        return ResponseEntity.ok(
            summaryService.summarize(templateId)
        );
    }
}
