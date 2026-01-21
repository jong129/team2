package dev.jpa.team2.checklist.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.admin.dto.AdminAiBaseTemplateDto;
import dev.jpa.team2.checklist.admin.dto.AdminAiBaseTemplateItemDto;
import dev.jpa.team2.checklist.admin.repository.ChecklistTemplateItemRepository;
import dev.jpa.team2.checklist.admin.repository.ChecklistTemplateRepository;
import dev.jpa.team2.checklist.enums.Yn;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import lombok.RequiredArgsConstructor;

/**
 * ========================================== AI 체크리스트 개선용 조회 Service
 *
 * - AI 개선 화면에서 기준이 되는 "기존 템플릿" 데이터를 조회 - 읽기 전용 (조회 전용)
 * ==========================================
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiTemplateQueryService {

  private final ChecklistTemplateRepository templateRepository;
  private final ChecklistTemplateItemRepository templateItemRepository;

  /**
   * ========================================== ✅ AI 개선 기준 템플릿 조회
   *
   * @param templateId 기존 체크리스트 템플릿 ID
   * @return AdminAiBaseTemplateDto
   *
   *         [조회 내용] - 템플릿 기본 정보 - 템플릿에 포함된 활성 아이템 목록
   *
   *         [주의] - 수정/저장 목적 아님 (비교용) ==========================================
   */
  public AdminAiBaseTemplateDto getBaseTemplate(Long templateId) {

    /*
     * ========================== 1. 템플릿 메타 조회 ==========================
     */
    ChecklistTemplate template = templateRepository.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 템플릿입니다. templateId=" + templateId));

    /*
     * ========================== 2. 템플릿 아이템 조회 - ACTIVE_YN = 'Y' - ITEM_ORDER 기준 정렬
     * ==========================
     */
    List<ChecklistTemplateItem> items =
        templateItemRepository
            .findByTemplate_TemplateIdOrderByItemOrderAsc(templateId);


    /*
     * ========================== 3. Entity → DTO 변환 ==========================
     */
    List<AdminAiBaseTemplateItemDto> itemDtos = items.stream().map(item -> AdminAiBaseTemplateItemDto.builder()
        // ✅ 아이템 마스터 PK 사용 (정석)
        .itemId(item.getItemMaster().getItemMasterId()).itemOrder(item.getItemOrder())
        .title(item.getItemMaster().getTitle()) // ⚠️ title은 마스터에 있음
        .description(item.getItemMaster().getDescription()) // 마스터 기준
        .requiredYn(item.getRequiredYn().name()).build()).collect(Collectors.toList());

    /*
     * ========================== 4. 최종 DTO 조립 ==========================
     */
    return AdminAiBaseTemplateDto.builder().templateId(template.getTemplateId())
        .templateName(template.getTemplateName()).versionNo(template.getVersionNo()).phase(template.getPhase())
        .postGroupCode(template.getPostGroupCode()).items(itemDtos).build();
  }
}
