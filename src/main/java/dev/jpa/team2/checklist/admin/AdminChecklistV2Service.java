package dev.jpa.team2.checklist.admin;

import dev.jpa.team2.checklist.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminChecklistV2Service {

  private final AdminItemMasterRepository itemMasterRepo;
  private final AdminTemplateItemRepository templateItemRepo;

  // 기존 템플릿 엔티티 조회용(이미 프로젝트에 있음)
  private final dev.jpa.team2.checklist.model.ChecklistTemplateRepository templateRepo;
  // ↑ 여기 “templateRepo”는 네가 이미 갖고 있는 JpaRepository<ChecklistTemplate,Long> 아무거나로 바꿔도 됨
  // 예: ChecklistTemplateRepository 같은 공용 repo가 있으면 그걸로 교체 추천

  public Page<ItemMasterRowDTO> listItemMasters(Phase phase, String postGroupCode, String activeYn, String keyword, Pageable pageable) {
    Page<ChecklistItemMaster> page = itemMasterRepo.search(phase, blankToNull(postGroupCode), blankToNull(activeYn), blankToNull(keyword), pageable);
    return page.map(m -> ItemMasterRowDTO.builder()
        .itemMasterId(m.getItemMasterId())
        .phase(m.getPhase())
        .postGroupCode(m.getPostGroupCode())
        .title(m.getTitle())
        .description(m.getDescription())
        .activeYn(m.getActiveYn())
        .createdAt(m.getCreatedAt())
        .updatedAt(m.getUpdatedAt())
        .build());
  }

  public List<TemplateItemRowDTO> getTemplateItems(Long templateId) {
    List<ChecklistTemplateItem> list =
        templateItemRepo.findByTemplate_TemplateIdAndActiveYnOrderByItemOrderAsc(templateId, "Y");
    return list.stream().map(ti -> TemplateItemRowDTO.builder()
        .itemMasterId(ti.getItemMaster().getItemMasterId())
        .itemOrder(ti.getItemOrder())
        .requiredYn(ti.getRequiredYn())
        .activeYn(ti.getActiveYn())
        .phase(ti.getItemMaster().getPhase())
        .postGroupCode(ti.getItemMaster().getPostGroupCode())
        .title(ti.getItemMaster().getTitle())
        .description(ti.getItemMaster().getDescription())
        .build()
    ).toList();
  }

  @Transactional
  public void replaceTemplateItems(Long templateId, List<TemplateItemUpsertReq> reqs) {
    if (reqs == null) reqs = List.of();

    ChecklistTemplate template = templateRepo.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("템플릿 없음: " + templateId));

    // order 중복 체크
    Set<Integer> orders = new HashSet<>();
    for (TemplateItemUpsertReq r : reqs) {
      if (r.getItemOrder() == null) throw new IllegalArgumentException("itemOrder 누락");
      if (!orders.add(r.getItemOrder())) throw new IllegalArgumentException("itemOrder 중복: " + r.getItemOrder());
    }

    // 마스터 존재 체크
    List<Long> ids = reqs.stream().map(TemplateItemUpsertReq::getItemMasterId).filter(Objects::nonNull).toList();
    Map<Long, ChecklistItemMaster> masters = itemMasterRepo.findAllById(ids).stream()
        .collect(Collectors.toMap(ChecklistItemMaster::getItemMasterId, Function.identity()));

    for (Long id : ids) {
      if (!masters.containsKey(id)) throw new IllegalArgumentException("항목 마스터 없음: " + id);
    }

    // 기존 구성 삭제 후 재삽입(프론트 구현 쉬움)
    templateItemRepo.deleteByTemplate_TemplateId(templateId);

    List<ChecklistTemplateItem> toSave = new ArrayList<>();
    for (TemplateItemUpsertReq r : reqs) {
      ChecklistItemMaster m = masters.get(r.getItemMasterId());

      ChecklistTemplateItem ti = ChecklistTemplateItem.builder()
          .id(new ChecklistTemplateItemId(template.getTemplateId(), m.getItemMasterId()))
          .template(template)
          .itemMaster(m)
          .itemOrder(r.getItemOrder())
          .requiredYn(nvlYn(r.getRequiredYn(), "Y"))
          .activeYn(nvlYn(r.getActiveYn(), "Y"))
          .build();

      toSave.add(ti);
    }
    templateItemRepo.saveAll(toSave);
  }

  private static String blankToNull(String s) {
    return (s == null || s.isBlank()) ? null : s.trim();
  }

  private static String nvlYn(String v, String def) {
    if (v == null || v.isBlank()) return def;
    String x = v.trim().toUpperCase();
    return (x.equals("Y") || x.equals("N")) ? x : def;
  }
  
  @Transactional
  public Long cloneTemplate(Long templateId) {
    ChecklistTemplate base = templateRepo.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("템플릿 없음: " + templateId));

    Phase phase = base.getPhase();
    String postGroupCode = base.getPostGroupCode();

    // 다음 버전 번호 계산
    int nextVersion;
    if (postGroupCode == null) {
      nextVersion = templateRepo.findFirstByPhaseAndPostGroupCodeIsNullOrderByVersionNoDesc(phase)
          .map(t -> t.getVersionNo() + 1)
          .orElse(1);
    } else {
      nextVersion = templateRepo.findFirstByPhaseAndPostGroupCodeOrderByVersionNoDesc(phase, postGroupCode)
          .map(t -> t.getVersionNo() + 1)
          .orElse(1);
    }

    // 템플릿명 v숫자 정리 후 v+1
    String baseName = base.getTemplateName().replaceAll("\\s*v\\d+$", "");
    ChecklistTemplate draft = ChecklistTemplate.builder()
        .phase(phase)
        .postGroupCode(postGroupCode)
        .templateName(baseName + " v" + nextVersion)
        .versionNo(nextVersion)
        .status(TemplateStatus.DRAFT)
        .description(base.getDescription())
        .createdAt(java.time.LocalDateTime.now())
        .updatedAt(java.time.LocalDateTime.now())
        .build();

    draft = templateRepo.save(draft);

    // 구성 복사 (CHECKLIST_TEMPLATE_ITEM)
    List<ChecklistTemplateItem> oldItems =
        templateItemRepo.findByTemplate_TemplateIdOrderByItemOrderAsc(base.getTemplateId());

    List<ChecklistTemplateItem> newItems = new ArrayList<>();
    for (ChecklistTemplateItem oi : oldItems) {
      ChecklistItemMaster m = oi.getItemMaster();

      ChecklistTemplateItem ti = ChecklistTemplateItem.builder()
          .id(new ChecklistTemplateItemId(draft.getTemplateId(), m.getItemMasterId()))
          .template(draft)
          .itemMaster(m)
          .itemOrder(oi.getItemOrder())
          .requiredYn(oi.getRequiredYn())
          .activeYn(oi.getActiveYn())
          .build();

      newItems.add(ti);
    }

    templateItemRepo.saveAll(newItems);

    return draft.getTemplateId();
  }

  @Transactional
  public void publishTemplate(Long templateId) {
    ChecklistTemplate draft = templateRepo.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("템플릿 없음: " + templateId));

    if (draft.getStatus() != TemplateStatus.DRAFT) {
      throw new IllegalArgumentException("DRAFT만 배포할 수 있습니다.");
    }

    Phase phase = draft.getPhase();
    String postGroupCode = draft.getPostGroupCode();

    // 같은 그룹의 기존 ACTIVE → RETIRED
    Optional<ChecklistTemplate> activeOpt;
    if (postGroupCode == null) {
      activeOpt = templateRepo.findFirstByPhaseAndPostGroupCodeIsNullAndStatusOrderByVersionNoDesc(
          phase, TemplateStatus.ACTIVE);
    } else {
      activeOpt = templateRepo.findFirstByPhaseAndPostGroupCodeAndStatusOrderByVersionNoDesc(
          phase, postGroupCode, TemplateStatus.ACTIVE);
    }

    activeOpt.ifPresent(active -> active.changeStatus(TemplateStatus.RETIRED));

    // draft → ACTIVE
    draft.changeStatus(TemplateStatus.ACTIVE);
  }

}
