package dev.jpa.team2.checklist.admin.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.admin.dto.AdminChecklistTemplateRowDto;
import dev.jpa.team2.checklist.admin.dto.TemplateItemDto;
import dev.jpa.team2.checklist.admin.dto.TemplateItemSaveDto;
import dev.jpa.team2.checklist.admin.dto.TemplateMetaDto;
import dev.jpa.team2.checklist.admin.dto.TemplateMetaUpdateDto;
import dev.jpa.team2.checklist.admin.dto.TemplateStatusUpdateDto;
import dev.jpa.team2.checklist.admin.service.AdminChecklistTemplateService;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 체크리스트 템플릿 관리 컨트롤러
 *
 * 담당 기능
 * 1. 템플릿 목록 조회
 * 2. 템플릿 단건 메타 조회
 * 3. 템플릿 구성 항목 조회
 * 4. 템플릿 구성 저장 (전체 교체)
 * 5. 템플릿 메타 수정
 * 6. 템플릿 상태 변경
 */
@RestController
@RequestMapping("/admin/checklists/templates")
@RequiredArgsConstructor
public class AdminChecklistTemplateController {

    private final AdminChecklistTemplateService templateService;

    /* =====================================================
     * 1. 템플릿 목록 조회
     * ===================================================== */

    /**
     * 체크리스트 템플릿 목록 조회 (관리자)
     *
     * GET /admin/checklists/templates/list
     */
    @GetMapping("/list")
    public Page<AdminChecklistTemplateRowDto> getTemplates(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "phase", required = false) String phase,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sortKey", defaultValue = "templateId") String sortKey,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir
    ) {
        return templateService.getTemplates(
                page,
                size,
                phase,
                status,
                keyword,
                sortKey,
                sortDir
        );
    }

    /* =====================================================
     * 2. 템플릿 단건 메타 조회
     * ===================================================== */

    /**
     * 템플릿 단건 메타 조회
     *
     * GET /admin/checklists/templates/{templateId}
     */
    @GetMapping("/{templateId}")
    public TemplateMetaDto getTemplateMeta(
            @PathVariable(name = "templateId") Long templateId
    ) {
        return templateService.getTemplateMeta(templateId);
    }

    /* =====================================================
     * 3. 템플릿 구성 항목 조회
     * ===================================================== */

    /**
     * 템플릿 구성 항목 조회
     *
     * GET /admin/checklists/templates/{templateId}/items
     */
    @GetMapping("/{templateId}/items")
    public List<TemplateItemDto> getTemplateItems(
            @PathVariable(name = "templateId") Long templateId
    ) {
        return templateService.getTemplateItems(templateId);
    }

    /* =====================================================
     * 4. 템플릿 구성 저장 (전체 교체)
     * ===================================================== */

    /**
     * 템플릿 구성 저장
     * - 기존 구성 전체 삭제 후 재등록
     * - DRAFT 상태에서만 허용
     *
     * PUT /admin/checklists/templates/{templateId}/items
     */
    @PutMapping("/{templateId}/items")
    public ResponseEntity<Void> saveTemplateItems(
            @PathVariable(name = "templateId") Long templateId,
            @RequestBody List<TemplateItemSaveDto> items
    ) {
        templateService.saveTemplateItems(templateId, items);
        return ResponseEntity.ok().build();
    }

    /* =====================================================
     * 5. 템플릿 메타 수정
     * ===================================================== */

    /**
     * 템플릿 메타 수정 (이름, 설명)
     * - DRAFT 상태에서만 허용
     *
     * PATCH /admin/checklists/templates/{templateId}/meta
     */
    @PatchMapping("/{templateId}/meta")
    public ResponseEntity<Void> updateTemplateMeta(
            @PathVariable(name = "templateId") Long templateId,
            @RequestBody TemplateMetaUpdateDto dto
    ) {
        templateService.updateTemplateMeta(templateId, dto);
        return ResponseEntity.ok().build();
    }

    /* =====================================================
     * 6. 템플릿 상태 변경
     * ===================================================== */

    /**
     * 템플릿 상태 변경
     *
     * PATCH /admin/checklists/templates/{templateId}/status
     */
    @PatchMapping("/{templateId}/status")
    public ResponseEntity<Void> updateTemplateStatus(
            @PathVariable(name = "templateId") Long templateId,
            @RequestBody TemplateStatusUpdateDto dto
    ) {
        templateService.updateTemplateStatus(templateId, dto.getStatus());
        return ResponseEntity.ok().build();
    }
    
    /* =====================================================
     * 7. 템플릿 완전 삭제 (물리 삭제)
     * ===================================================== */

    /**
     * 템플릿 완전 삭제 (물리 삭제)
     *
     * DELETE /admin/checklists/templates/{templateId}
     */
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @PathVariable(name = "templateId") Long templateId
    ) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }

}
