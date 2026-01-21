package dev.jpa.team2.checklist.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.admin.dto.AdminPostTemplateAnalysisDto;
import dev.jpa.team2.checklist.admin.service.AdminPostChecklistAnalysisService;
import lombok.RequiredArgsConstructor;

/**
 * 관리자용 POST 체크리스트 템플릿 분석 Controller
 *
 * - 관리자 AI 패널에서 사용
 * - 특정 POST 템플릿의 만족도/코멘트 요약 제공
 */
@RestController
@RequestMapping("/admin/checklists/post/templates")
@RequiredArgsConstructor
public class AdminPostChecklistAnalysisController {

    private final AdminPostChecklistAnalysisService analysisService;

    /**
     * POST 템플릿 만족도 분석 조회
     *
     * GET /admin/checklists/post/templates/{templateId}/analysis
     */
    @GetMapping("/{templateId}/analysis")
    public ResponseEntity<AdminPostTemplateAnalysisDto> analyze(
        @PathVariable(name = "templateId") Long templateId
    ) {
        return ResponseEntity.ok(
            analysisService.analyze(templateId)
        );
    }
}
