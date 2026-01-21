package dev.jpa.team2.checklist.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.admin.dto.AdminAiImproveSummaryRequestDto;
import dev.jpa.team2.checklist.admin.dto.AdminAiImproveSummaryResponseDto;
import dev.jpa.team2.checklist.admin.service.AiTemplateImproveSummaryService;
import lombok.RequiredArgsConstructor;

/**
 * ==========================================
 * 관리자 AI 체크리스트 개선 요약 Controller
 *
 * - 개선 템플릿 "미리보기" 이후
 * - 왜 해당 항목들이 추가/변경되었는지 설명
 * ==========================================
 */
@RestController
@RequestMapping("/admin/checklists/ai")
@RequiredArgsConstructor
public class AdminChecklistAiImproveSummaryController {

    private final AiTemplateImproveSummaryService summaryService;

    /**
     * ==========================================
     * ✅ AI 개선 요약 생성
     *
     * POST /admin/checklists/ai/improve/summary
     *
     * [요청 데이터]
     * - 미리보기 결과 항목
     * - 항목별 사용자 수행 통계
     * - 만족도 요약 정보
     *
     * [응답 데이터]
     * - 항목별 개선 사유 설명
     * ==========================================
     */
    @PostMapping("/improve/summary")
    public ResponseEntity<AdminAiImproveSummaryResponseDto> improveSummary(
            @RequestBody AdminAiImproveSummaryRequestDto request
    ) {

        AdminAiImproveSummaryResponseDto result =
                summaryService.summarize(request);

        return ResponseEntity.ok(result);
    }
}
