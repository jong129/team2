package dev.jpa.team2.checklist.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.admin.dto.AdminChecklistTemplateRowDto;
import dev.jpa.team2.checklist.admin.dto.TemplateItemDto;
import dev.jpa.team2.checklist.admin.dto.TemplateItemSaveDto;
import dev.jpa.team2.checklist.admin.dto.TemplateMetaDto;
import dev.jpa.team2.checklist.admin.dto.TemplateMetaUpdateDto;
import dev.jpa.team2.checklist.admin.repository.ChecklistItemMasterRepository;
import dev.jpa.team2.checklist.admin.repository.ChecklistTemplateItemRepository;
import dev.jpa.team2.checklist.admin.repository.ChecklistTemplateRepository;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.TemplateStatus;
import dev.jpa.team2.checklist.model.ChecklistItemMaster;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import dev.jpa.team2.checklist.model.ChecklistTemplateItemId;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 체크리스트 템플릿 관리 서비스
 *
 * 담당 기능 1. 템플릿 목록 조회 2. 템플릿 단건 메타 조회 3. 템플릿 구성 항목 조회 4. 템플릿 구성 저장 (전체 교체) 5.
 * 템플릿 메타 수정 6. 템플릿 상태 변경
 */
@Service
@RequiredArgsConstructor
public class AdminChecklistTemplateService {

  private final ChecklistTemplateRepository templateRepository;
  private final ChecklistTemplateItemRepository templateItemRepository;
  private final ChecklistItemMasterRepository itemMasterRepository;

  /*
   * ===================================================== 1. 템플릿 목록 조회
   * =====================================================
   */

  @Transactional(readOnly = true)
  public Page<AdminChecklistTemplateRowDto> getTemplates(int page, int size, String phase, String status,
      String keyword, String sortKey, String sortDir) {

    // 1️⃣ 정렬 (sortKey / sortDir 기준)
    Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC, sortKey);

    // 2️⃣ Pageable 생성
    Pageable pageable = PageRequest.of(page, size, sort);

    // 3️⃣ String → Enum 변환
    ChecklistPhase phaseEnum = null;
    TemplateStatus statusEnum = null;

    if (phase != null && !phase.isBlank()) {
      phaseEnum = ChecklistPhase.valueOf(phase);
    }

    if (status != null && !status.isBlank()) {
      statusEnum = TemplateStatus.valueOf(status);
    }

    // 4️⃣ Repository 호출
    return templateRepository.findAdminTemplateList(phaseEnum, statusEnum, keyword, pageable);
  }

  /*
   * ===================================================== 2. 템플릿 단건 메타 조회
   * =====================================================
   */

  @Transactional(readOnly = true)
  public TemplateMetaDto getTemplateMeta(Long templateId) {

    ChecklistTemplate template = templateRepository.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("템플릿이 존재하지 않습니다."));

    TemplateMetaDto dto = new TemplateMetaDto();
    dto.setTemplateId(template.getTemplateId());
    dto.setTemplateName(template.getTemplateName());
    dto.setDescription(template.getDescription());
    dto.setPhase(template.getPhase());
    dto.setVersionNo(template.getVersionNo());
    dto.setStatus(template.getStatus());
    dto.setPostGroupCode(template.getPostGroupCode());

    return dto;
  }

  /*
   * ===================================================== 3. 템플릿 구성 항목 조회
   * =====================================================
   */

  @Transactional(readOnly = true)
  public List<TemplateItemDto> getTemplateItems(Long templateId) {

    List<ChecklistTemplateItem> items = templateItemRepository.findByTemplate_TemplateIdOrderByItemOrderAsc(templateId);

    return items.stream().map(item -> {
      ChecklistItemMaster master = item.getItemMaster();

      TemplateItemDto dto = new TemplateItemDto();
      dto.setItemMasterId(master.getItemMasterId());
      dto.setItemOrder(item.getItemOrder());
      dto.setRequiredYn(item.getRequiredYn());
      dto.setActiveYn(item.getActiveYn());
      dto.setPhase(master.getPhase());
      dto.setPostGroupCode(master.getPostGroupCode());
      dto.setTitle(master.getTitle());
      dto.setDescription(master.getDescription());

      return dto;
    }).collect(Collectors.toList());
  }

  /*
   * ===================================================== 4. 템플릿 구성 저장 (전체 교체)
   * =====================================================
   */

  @Transactional
  public void saveTemplateItems(Long templateId, List<TemplateItemSaveDto> items) {

    ChecklistTemplate template = templateRepository.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("템플릿이 존재하지 않습니다."));

    if (template.getStatus() == TemplateStatus.ACTIVE) {
      throw new IllegalStateException("활성화된 템플릿은 수정할 수 없습니다.");
    }

    // 1️⃣ 기존 구성 전체 삭제
    templateItemRepository.deleteByTemplate_TemplateId(templateId);

    // 2️⃣ 새 구성 저장
    for (TemplateItemSaveDto dto : items) {

      ChecklistItemMaster master = itemMasterRepository.findById(dto.getItemMasterId())
          .orElseThrow(() -> new IllegalArgumentException("항목 마스터가 존재하지 않습니다."));

      // =========================
      // ✅ 1. 복합 PK 객체 생성 (핵심)
      // =========================
      ChecklistTemplateItemId id = new ChecklistTemplateItemId();
      id.setTemplateId(template.getTemplateId());
      id.setItemMasterId(master.getItemMasterId());

      // =========================
      // ✅ 2. 엔티티 생성 + ID 세팅
      // =========================
      ChecklistTemplateItem entity = new ChecklistTemplateItem();
      entity.setId(id); // ⭐⭐⭐ 반드시 필요

      // =========================
      // ✅ 3. 연관관계 세팅 (@MapsId)
      // =========================
      entity.setTemplate(template);
      entity.setItemMaster(master);

      // =========================
      // ✅ 4. 일반 컬럼 세팅
      // =========================
      entity.setItemOrder(dto.getItemOrder());
      entity.setRequiredYn(dto.getRequiredYn());
      entity.setActiveYn(dto.getActiveYn());

      // =========================
      // ✅ 5. 저장
      // =========================
      templateItemRepository.save(entity);
    }
  }

  /*
   * ===================================================== 5. 템플릿 메타 수정
   * =====================================================
   */

  @Transactional
  public void updateTemplateMeta(Long templateId, TemplateMetaUpdateDto dto) {

    ChecklistTemplate template = templateRepository.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("템플릿이 존재하지 않습니다."));

    if (template.getStatus() == TemplateStatus.ACTIVE) {
      throw new IllegalStateException("활성화된 템플릿은 수정할 수 없습니다.");
    }

    template.setTemplateName(dto.getTemplateName());
    template.setDescription(dto.getDescription());
  }

  /*
   * ===================================================== 6. 템플릿 상태 변경
   * =====================================================
   */

  @Transactional
  public void updateTemplateStatus(Long templateId, TemplateStatus status) {

    ChecklistTemplate template = templateRepository.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("템플릿이 존재하지 않습니다."));

    template.setStatus(status);
  }
  
  /*
   * =====================================================
   * 7. 템플릿 완전 삭제 (물리 삭제)
   * =====================================================
   */
  @Transactional
  public void deleteTemplate(Long templateId) {

    ChecklistTemplate template = templateRepository.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("템플릿이 존재하지 않습니다."));

    // 🔒 PRE 템플릿은 정책상 물리 삭제 금지
    if (template.getPhase() == ChecklistPhase.PRE) {
      throw new IllegalStateException("사전 체크리스트 템플릿은 삭제할 수 없습니다.");
    }

    // 1️⃣ 템플릿-항목 매핑 먼저 삭제
    templateItemRepository.deleteByTemplate_TemplateId(templateId);

    // 2️⃣ 템플릿 삭제
    templateRepository.delete(template);
  }
  
  

}
