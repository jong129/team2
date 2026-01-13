package dev.jpa.team2.checklist.ai;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.model.ChecklistItem;
import dev.jpa.team2.checklist.model.ChecklistItemMaster;
import dev.jpa.team2.checklist.model.ChecklistItemMasterRepository;
import dev.jpa.team2.checklist.model.ChecklistItemRepository;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import dev.jpa.team2.checklist.model.ChecklistTemplateItemId;
import dev.jpa.team2.checklist.model.ChecklistTemplateItemRepository;
import dev.jpa.team2.checklist.model.ChecklistTemplateRepository;
import dev.jpa.team2.checklist.model.Phase;
import dev.jpa.team2.checklist.model.TemplateStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminPostChecklistAiImproveService {

  private final ChecklistTemplateRepository templateRepo;
  private final ChecklistTemplateItemRepository templateItemRepo;
  private final ChecklistItemRepository itemRepo;

  private final AiPostChecklistService aiPostChecklistService;
  private final ChecklistItemMasterRepository masterRepo;

  /**
   * ✅ 시그널 반영 개선 초안 생성 (룰 기반)
   * - REMOVE_CANDIDATE: 제외
   * - IMPROVE_COPY: 같은 CHECK_AREA 내에서 다른 master로 교체(우리 마스터 풀 내에서만)
   * - KEEP/INSIGHT_CANDIDATE: 유지 (INSIGHT는 다음 단계에서 required 조정 가능)
   */
  public ImproveResult createDraftFromBaseTemplate(Long baseTemplateId) {

    // 1) base 템플릿 조회
    ChecklistTemplate base = templateRepo.findById(baseTemplateId)
        .orElseThrow(() -> new IllegalArgumentException("baseTemplateId not found: " + baseTemplateId));

    // 2) 안전장치: POST + ACTIVE만 허용
    if (base.getPhase() != Phase.POST) throw new IllegalStateException("POST 템플릿만 AI 개선 대상으로 허용합니다.");
    if (base.getStatus() != TemplateStatus.ACTIVE) throw new IllegalStateException("ACTIVE 템플릿만 AI 개선 대상으로 허용합니다.");

    // 3) base 조립 목록(마스터 포함, 순서 포함)
    List<ChecklistTemplateItem> baseTis =
        templateItemRepo.findActiveItemsByTemplateIdOrderByItemOrder(baseTemplateId);

    if (baseTis.isEmpty()) throw new IllegalStateException("base 템플릿에 활성 항목이 없습니다.");

    // 4) base 런타임 CHECKLIST_ITEM (signal DTO가 itemId 기반이므로 필요)
    List<ChecklistItem> baseRuntime =
        itemRepo.findByTemplate_TemplateIdOrderByItemOrderAsc(baseTemplateId);

    // itemOrder -> runtime row 매핑(신뢰도 높음)
    Map<Integer, ChecklistItem> runtimeByOrder = baseRuntime.stream()
        .collect(Collectors.toMap(ChecklistItem::getItemOrder, x -> x, (a, b) -> a));

    // 5) 시그널 로드 (이미 존재하는 메서드명 사용)
    List<AiPostItemSignalDTO> signals = aiPostChecklistService.getItemSignals(baseTemplateId);
    Map<Long, AiPostItemSignalDTO> signalByItemId = signals.stream()
        .filter(s -> s.getItemId() != null)
        .collect(Collectors.toMap(AiPostItemSignalDTO::getItemId, s -> s, (a, b) -> a));

    // 6) 마스터 풀(우리 DB 내 신뢰 가능한 항목) 로드: 동일 phase/postGroupCode + active만
    //    ✅ Repository 메서드명은 아래 예시로 가정. 네 repo에 맞춰줄게(없으면 바로 만들어주면 됨).
    List<ChecklistItemMaster> masterPool =
        masterRepo.findActiveMasters(base.getPhase(), base.getPostGroupCode());

    if (masterPool.isEmpty()) {
      throw new IllegalStateException("마스터 풀(교체 후보)이 비어있습니다. ITEM_MASTER를 먼저 채워주세요.");
    }

    Map<String, List<ChecklistItemMaster>> poolByArea = masterPool.stream()
        .collect(Collectors.groupingBy(m -> nvl(m.getCheckArea(), "_")));

    // 7) 새 버전 번호 = 같은 그룹에서 max + 1
    Integer maxVer = templateRepo.findMaxVersionNo(base.getPhase(), base.getPostGroupCode());
    int newVer = (maxVer == null ? 0 : maxVer) + 1;

    LocalDateTime now = LocalDateTime.now();

    // 8) 새 템플릿(DRAFT) 생성
    ChecklistTemplate draft = ChecklistTemplate.builder()
        .phase(base.getPhase())
        .postGroupCode(base.getPostGroupCode())
        .templateName(base.getTemplateName() + " (AI 초안)")
        .versionNo(newVer)
        .status(TemplateStatus.DRAFT)
        .description("AI 개선 초안 (기준 템플릿 ID=" + baseTemplateId + ")")
        .createdAt(now)
        .updatedAt(now)
        .build();

    draft = templateRepo.save(draft);

    // ✅ 혹시 모를 재시도 대비(보험)
    itemRepo.deleteByTemplate_TemplateId(draft.getTemplateId());

    // 9) 개선 규칙 적용하여 새 템플릿 아이템 생성
    Set<Long> usedMasterIds = new HashSet<>();

    for (ChecklistTemplateItem baseTi : baseTis) {

      Integer order = baseTi.getItemOrder();
      ChecklistItem rt = runtimeByOrder.get(order);

      PostChecklistSignalType signalType = PostChecklistSignalType.KEEP;
      if (rt != null) {
        AiPostItemSignalDTO sig = signalByItemId.get(rt.getItemId());
        if (sig != null && sig.getSignal() != null) signalType = sig.getSignal();
      }

      // base master
      ChecklistItemMaster baseMaster = baseTi.getItemMaster();
      String area = nvl(baseMaster.getCheckArea(), "_");

      // (1) REMOVE면 제외
      if (signalType == PostChecklistSignalType.REMOVE_CANDIDATE) {
        continue;
      }

      // (2) IMPROVE_COPY면 같은 영역에서 교체 시도
      ChecklistItemMaster chosen = baseMaster;
      if (signalType == PostChecklistSignalType.IMPROVE_COPY) {
        chosen = pickReplacement(area, baseMaster.getItemMasterId(), poolByArea, usedMasterIds);
      }

      // 중복 방지: 이미 사용중이면 다른 후보로 다시 뽑기
      if (usedMasterIds.contains(chosen.getItemMasterId())) {
        chosen = pickReplacement(area, chosen.getItemMasterId(), poolByArea, usedMasterIds);
      }

      usedMasterIds.add(chosen.getItemMasterId());

      // requiredYn은 일단 base 유지 (INSIGHT 반영은 다음 단계)
      String requiredYn =
          (signalType == PostChecklistSignalType.INSIGHT_CANDIDATE)
              ? "Y"
              : baseTi.getRequiredYn();


      // (A) CHECKLIST_TEMPLATE_ITEM 저장
      ChecklistTemplateItemId newId = new ChecklistTemplateItemId(
          draft.getTemplateId(),
          chosen.getItemMasterId()
      );

      ChecklistTemplateItem ni = ChecklistTemplateItem.builder()
          .id(newId)
          .template(draft)
          .itemMaster(chosen)
          .itemOrder(order)
          .requiredYn(requiredYn)
          .activeYn("Y")
          .createdAt(now)
          .updatedAt(now)
          .build();

      templateItemRepo.save(ni);

      // (B) 런타임 CHECKLIST_ITEM 저장
      ChecklistItem newRt = ChecklistItem.builder()
          .template(draft)
          .itemOrder(order)
          .checkArea(chosen.getCheckArea())
          .title(chosen.getTitle())
          .description(chosen.getDescription())
          .requiredYn(requiredYn)
          .activeYn("Y")
          .createdAt(now)
          .updatedAt(now)
          .build();

      itemRepo.save(newRt);
    }

    return new ImproveResult(baseTemplateId, draft.getTemplateId(), newVer);
  }

  private ChecklistItemMaster pickReplacement(
      String area,
      Long excludeMasterId,
      Map<String, List<ChecklistItemMaster>> poolByArea,
      Set<Long> usedMasterIds
  ) {
    List<ChecklistItemMaster> pool = poolByArea.getOrDefault(nvl(area, "_"), List.of());

    // 1순위: exclude 제외 + 미사용
    Optional<ChecklistItemMaster> c1 = pool.stream()
        .filter(m -> !Objects.equals(m.getItemMasterId(), excludeMasterId))
        .filter(m -> !usedMasterIds.contains(m.getItemMasterId()))
        .findFirst();
    if (c1.isPresent()) return c1.get();

    // 2순위: exclude 제외 (중복 허용)
    Optional<ChecklistItemMaster> c2 = pool.stream()
        .filter(m -> !Objects.equals(m.getItemMasterId(), excludeMasterId))
        .findFirst();
    if (c2.isPresent()) return c2.get();

    // 3순위: area 내 아무거나(최후)
    return pool.stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("교체 후보가 없습니다. area=" + area));
  }

  private static String nvl(String s, String d) {
    return (s == null || s.isBlank()) ? d : s;
  }

  public record ImproveResult(Long baseTemplateId, Long newTemplateId, Integer newVersionNo) {}
}
