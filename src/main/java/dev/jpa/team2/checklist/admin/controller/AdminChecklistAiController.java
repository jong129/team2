package dev.jpa.team2.checklist.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.admin.dto.AdminAiBaseTemplateDto;
import dev.jpa.team2.checklist.admin.dto.AdminAiImprovePreviewRequestDto;
import dev.jpa.team2.checklist.admin.dto.AdminAiImprovePreviewResponseDto;
import dev.jpa.team2.checklist.admin.dto.AdminAiImproveSaveRequestDto;
import dev.jpa.team2.checklist.admin.service.AiTemplateImprovePreviewService;
import dev.jpa.team2.checklist.admin.service.AiTemplateImproveSaveService;
import dev.jpa.team2.checklist.admin.service.AiTemplateQueryService;
import lombok.RequiredArgsConstructor;

/**
 * ================================
 * 관리자 AI 체크리스트 Controller (최종)
 *
 * - AI 기반 체크리스트 개선 화면 전용 API
 * - 기존 템플릿 조회
 * - AI 개선 미리보기 (RAG 연동)
 * ================================
 */
@RestController
@RequestMapping("/admin/checklists/ai")
@RequiredArgsConstructor
public class AdminChecklistAiController {

    private final AiTemplateQueryService aiTemplateQueryService;
    private final AiTemplateImprovePreviewService previewService;
    private final AiTemplateImproveSaveService saveService;


    /**
     * =========================================
     * ✅ AI 개선 기준이 되는 "기존 템플릿" 조회
     *
     * [용도]
     * - 좌측 "기존 템플릿" 영역
     *
     * GET /admin/checklists/ai/templates/{templateId}/base
     * =========================================
     */
    @GetMapping("/templates/{templateId}/base")
    public ResponseEntity<AdminAiBaseTemplateDto> getAiBaseTemplate(
            @PathVariable(name = "templateId") Long templateId
    ) {

        AdminAiBaseTemplateDto result =
                aiTemplateQueryService.getBaseTemplate(templateId);

        return ResponseEntity.ok(result);
    }

    /**
     * ==========================================
     * ✅ AI 개선 템플릿 미리보기 (RAG 연동)
     *
     * [용도]
     * - 우측 "AI 개선 템플릿" 영역
     * - FastAPI(RAG) + 기존 템플릿 병합 결과 반환
     *
     * POST /admin/checklists/ai/templates/{templateId}/preview
     * ==========================================
     */
    @PostMapping("/templates/{templateId}/preview")
    public ResponseEntity<AdminAiImprovePreviewResponseDto> preview(
            @PathVariable(name = "templateId") Long templateId,
            @RequestBody AdminAiImprovePreviewRequestDto request
    ) {

        AdminAiImprovePreviewResponseDto result =
                previewService.previewImproveTemplate(templateId, request);

        return ResponseEntity.ok(result);
    }
    
    /**
     * ==========================================
     * ✅ AI 개선 템플릿 초안 저장
     *
     * [용도]
     * - 관리자가 검토 완료 후 "저장하기" 클릭
     * - 새로운 템플릿을 DRAFT 상태로 생성
     *
     * POST /admin/checklists/ai/templates/{templateId}/save
     * ==========================================
     */
    @PostMapping("/templates/{templateId}/save")
    public ResponseEntity<Void> saveAiImprovedTemplate(
            @PathVariable(name = "templateId") Long templateId,
            @RequestBody AdminAiImproveSaveRequestDto request
    ) {

        saveService.saveDraft(templateId, request);

        return ResponseEntity.ok().build();
    }

}
