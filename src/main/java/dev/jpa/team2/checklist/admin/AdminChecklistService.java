package dev.jpa.team2.checklist.admin;

import java.time.LocalDateTime;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.ChecklistTemplateRepository;
import dev.jpa.team2.checklist.model.Phase;
import dev.jpa.team2.checklist.model.TemplateStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminChecklistService {

  private final AdminChecklistTemplateListRepository repo;      // 뷰 조회용(목록)
  private final ChecklistTemplateRepository templateRepo;       // 엔티티 수정용(CRUD)

  private final AdminTemplateItemRepository templateItemRepo;   // ✅ 추가

  public Page<AdminTemplateRow> listTemplates(
      String phase, String status, String keyword,
      String sortKey, String sortDir,
      Pageable pageable
  ) {
    String p = (phase == null || phase.isBlank()) ? null : phase.trim();
    String s = (status == null || status.isBlank()) ? null : status.trim();
    String k = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

    // ✅ 화이트리스트 (보안 + 안정성)
    String key = (sortKey == null || sortKey.isBlank()) ? "updatedAt" : sortKey.trim();
    String dir = (sortDir == null || sortDir.isBlank()) ? "desc" : sortDir.trim().toLowerCase();

    if (!key.equals("templateId") && !key.equals("updatedAt")) key = "updatedAt";
    if (!dir.equals("asc") && !dir.equals("desc")) dir = "desc";

    Page<AdminTemplateRow> page = repo.findAdminTemplateRows(p, s, k, key, dir, pageable);

    return page.map(r -> {
      long itemCntL = templateItemRepo.countByTemplate_TemplateId(r.getTemplateId());
      long activeItemCntL = templateItemRepo.countByTemplate_TemplateIdAndActiveYn(r.getTemplateId(), "Y");

      return new AdminTemplateRowDTO(
          r.getTemplateId(),
          r.getPhase(),
          r.getPostGroupCode(),
          r.getTemplateName(),
          r.getVersionNo(),
          r.getStatus(),
          r.getDescription(),
          r.getCreatedAt(),
          r.getUpdatedAt(),
          Math.toIntExact(activeItemCntL),
          Math.toIntExact(itemCntL)
      );
    });
  }


  @Transactional
  public void updateTemplateStatus(Long templateId, String status) {
    ChecklistTemplate t = templateRepo.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("템플릿 없음: " + templateId));

    TemplateStatus newStatus = TemplateStatus.valueOf(status);
    t.changeStatus(newStatus);
  }

  public AdminTemplateDetailDTO getTemplate(Long templateId) {
    ChecklistTemplate t = templateRepo.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("템플릿 없음: " + templateId));

    return AdminTemplateDetailDTO.builder()
        .templateId(t.getTemplateId())
        .phase(t.getPhase())
        .postGroupCode(t.getPostGroupCode())
        .templateName(t.getTemplateName())
        .versionNo(t.getVersionNo())
        .status(t.getStatus())
        .description(t.getDescription())
        .createdAt(t.getCreatedAt())
        .updatedAt(t.getUpdatedAt())
        .build();
  }

  @Transactional
  public void updateTemplateMeta(Long templateId, AdminTemplateMetaUpdateReq req) {
    ChecklistTemplate t = templateRepo.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("템플릿 없음: " + templateId));

    if (req.getTemplateName() != null) t.setTemplateName(req.getTemplateName().trim());
    if (req.getDescription() != null) t.setDescription(req.getDescription().trim());

    // (너가 말한 정책대로면 여기 status는 안 받게 하는 게 더 깔끔함)
    if (req.getStatus() != null) t.setStatus(req.getStatus());

    t.setUpdatedAt(java.time.LocalDateTime.now());
  }
  
  @Transactional
  public Long createTemplate(AdminTemplateCreateReq req) {

    // 1) 필수값 검증
    if (req.getPhase() == null || req.getPhase().isBlank()) {
      throw new IllegalArgumentException("phase는 필수입니다.");
    }
    if (req.getTemplateName() == null || req.getTemplateName().isBlank()) {
      throw new IllegalArgumentException("templateName은 필수입니다.");
    }

    String phase = req.getPhase().trim().toUpperCase();
    Phase phaseEnum = Phase.valueOf(phase);
    String name = req.getTemplateName().trim();

    String desc = (req.getDescription() == null || req.getDescription().isBlank())
        ? null : req.getDescription().trim();

    String postGroupCode = (req.getPostGroupCode() == null || req.getPostGroupCode().isBlank())
        ? null : req.getPostGroupCode().trim().toUpperCase();

    // POST면 postGroupCode 필수
    if ("POST".equals(phase) && postGroupCode == null) {
      throw new IllegalArgumentException("POST 단계는 postGroupCode가 필수입니다.");
    }

    // PRE면 postGroupCode는 null로 정리
    if ("PRE".equals(phase)) {
      postGroupCode = null;
    }

    // 2) versionNo 자동 산정 (같은 phase + postGroupCode 내 max+1)
    Integer maxVer = templateRepo.findMaxVersionNo(phaseEnum, postGroupCode);
    int nextVer = (maxVer == null) ? 1 : maxVer + 1;

    ChecklistTemplate t = ChecklistTemplate.builder()
        .phase(phaseEnum)
        .postGroupCode(postGroupCode)
        .templateName(name)
        .description(desc)
        .versionNo(nextVer)
        .status(TemplateStatus.DRAFT)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    
    ChecklistTemplate saved = templateRepo.save(t);
    return saved.getTemplateId();
  }


}
