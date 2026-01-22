package dev.jpa.team2.checklist.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.admin.dto.AdminAiImproveSaveRequestDto;
import dev.jpa.team2.checklist.admin.dto.AdminAiImproveSaveRequestDto.PreviewItemDto;
import dev.jpa.team2.checklist.admin.repository.ChecklistItemMasterRepository;
import dev.jpa.team2.checklist.admin.repository.ChecklistTemplateItemRepository;
import dev.jpa.team2.checklist.admin.repository.ChecklistTemplateRepository;
import dev.jpa.team2.checklist.enums.TemplateStatus;
import dev.jpa.team2.checklist.enums.Yn;
import dev.jpa.team2.checklist.model.ChecklistItemMaster;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import dev.jpa.team2.checklist.model.ChecklistTemplateItemId;
import lombok.RequiredArgsConstructor;

/**
 * ========================================== AI 개선 템플릿 저장 Service
 *
 * - AI 미리보기 결과를 "DRAFT 템플릿"으로 DB에 저장 - 신규 ItemMaster 생성 후 템플릿에 매핑
 * ==========================================
 */
@Service
@RequiredArgsConstructor
public class AiTemplateImproveSaveService {

  private final ChecklistTemplateRepository templateRepository;
  private final ChecklistTemplateItemRepository templateItemRepository;
  private final ChecklistItemMasterRepository itemMasterRepository;

  /**
   * ========================================== AI 개선 템플릿 초안 저장
   * ==========================================
   */
  @Transactional
  public void saveDraft(Long baseTemplateId, AdminAiImproveSaveRequestDto request) {

    /*
     * ========================== 1️⃣ 기준 템플릿 조회 ==========================
     */
    ChecklistTemplate baseTemplate = templateRepository.findById(baseTemplateId)
        .orElseThrow(() -> new IllegalArgumentException("기준 템플릿이 존재하지 않습니다."));

    /*
     * ========================== 2️⃣ 신규 템플릿 생성 (DRAFT) ==========================
     */
    ChecklistTemplate newTemplate = new ChecklistTemplate();
    newTemplate.setPhase(baseTemplate.getPhase());
    newTemplate.setPostGroupCode(baseTemplate.getPostGroupCode());
    newTemplate.setTemplateName(baseTemplate.getTemplateName() + " (AI 개선 초안)");
    newTemplate.setVersionNo(baseTemplate.getVersionNo() + 1);
    newTemplate.setStatus(TemplateStatus.DRAFT);

    templateRepository.save(newTemplate);

    /*
     * ========================== 3️⃣ AI 항목 → ItemMaster + TemplateItem 생성
     * ==========================
     */
    List<PreviewItemDto> items = request.getItems();

    for (PreviewItemDto item : items) {

      /*
       * ========================== 3-1. ItemMaster 조회 또는 생성
       * ==========================
       */
      ChecklistItemMaster itemMaster = itemMasterRepository
          .findFirstByPhaseAndPostGroupCodeAndTitleOrderByItemMasterIdDesc(newTemplate.getPhase().name(),
              newTemplate.getPostGroupCode(), item.getTitle())
          .orElseGet(() -> {
            // 🆕 기존에 없을 때만 신규 생성
            ChecklistItemMaster newItem = new ChecklistItemMaster();
            newItem.setPhase(newTemplate.getPhase().name());
            newItem.setPostGroupCode(newTemplate.getPostGroupCode());
            newItem.setTitle(item.getTitle());
            newItem.setDescription(item.getDescription());
            newItem.setActiveYn("Y");
            return itemMasterRepository.save(newItem);
          });

      /*
       * ========================== 3-2. 복합 PK 생성 ==========================
       */
      ChecklistTemplateItemId id = new ChecklistTemplateItemId();
      id.setTemplateId(newTemplate.getTemplateId());
      id.setItemMasterId(itemMaster.getItemMasterId());

      /*
       * ========================== 3-3. REQUIRED_YN 변환 (핵심 수정)
       * ==========================
       */
      Yn requiredYn = Yn.N; // 기본값

      if ("Y".equalsIgnoreCase(item.getRequiredYn())) {
        requiredYn = Yn.Y;
      }

      /*
       * ========================== 3-4. TemplateItem 생성 ==========================
       */
      ChecklistTemplateItem templateItem = new ChecklistTemplateItem();
      templateItem.setId(id);
      templateItem.setTemplate(newTemplate);
      templateItem.setItemMaster(itemMaster);
      templateItem.setItemOrder(item.getItemOrder());
      templateItem.setRequiredYn(requiredYn); // ✅ enum
      templateItem.setActiveYn(Yn.Y);

      templateItemRepository.save(templateItem);
    }
  }
}
