package dev.jpa.team2.checklist.post;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.model.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PostChecklistService {

  private final PostChecklistTemplateRepository postTemplateRepo;
  private final PostChecklistItemRepository itemRepo;
  private final PostChecklistSessionRepository sessionRepo;
  private final PostChecklistResponseRepository responseRepo;

  // model 패키지 공용 레포
  private final UserPreProfileKeyRepository profileKeyRepo;
  private final PostTemplateAssignmentRepository assignmentRepo;

  /** PRE 핵심 신호 문항 ITEM_ORDER */
  private static final List<Integer> SIGNAL_ORDERS = List.of(6, 7, 8, 12, 13, 14);

  /** POST 그룹 코드 */
  private static final String POST_A = "POST_A";
  private static final String POST_B = "POST_B";
  private static final String POST_C = "POST_C";
  private static final String POST_D = "POST_D";

  /** POST 세션 시작 */
  public PostChecklistDTO startPostSession(Long memberId, Long preSessionId) {

    // ✅ 0) 이미 진행중인 POST 세션이 있으면 재사용 (중복 생성 방지)
    Optional<ChecklistSession> existing = sessionRepo
        .findTopByMemberIdAndPhaseAndStatusAndDeletedYnOrderByStartedAtDesc(
            memberId, Phase.POST, "IN_PROGRESS", "N"
        );

    if (existing.isPresent()) {
      ChecklistSession s = existing.get();

      Long templateId = s.getTemplate().getTemplateId();
      ChecklistTemplate full = postTemplateRepo.findById(templateId)
          .orElseThrow(() -> new IllegalArgumentException("POST 템플릿 없음: " + templateId));

      return PostChecklistDTO.builder()
          .sessionId(s.getSessionId())
          .templateId(templateId)
          .postGroupCode(full.getPostGroupCode())
          .profileKeyId(s.getProfileKeyId())
          .build();
    }

    // ✅ 1) PRE 세션 Optional로 조회 (없을 수도 있음)
    Optional<ChecklistSession> preOpt;
    if (preSessionId == null) {
      preOpt = sessionRepo.findTopByMemberIdAndPhaseAndDeletedYnOrderByStartedAtDesc(memberId, Phase.PRE, "N");
    } else {
      preOpt = sessionRepo.findBySessionIdAndDeletedYn(preSessionId, "N");
    }

    // ✅ 2) PRE 세션이 없으면: 기본 profileKey 생성 + 기본 POST 세션 생성(제약조건 회피)
    if (preOpt.isEmpty()) {

      // ✅ 기본 신호(전부 DONE) -> route() 결과 POST_A
      Map<String, String> signals = new LinkedHashMap<>();
      signals.put("GAPGU_RISK", CheckStatus.DONE.name());
      signals.put("EULGU_MORTGAGE", CheckStatus.DONE.name());
      signals.put("PRIOR_SAFE", CheckStatus.DONE.name());
      signals.put("BUILDING_ILLEGAL", CheckStatus.DONE.name());
      signals.put("MOVEIN_OK", CheckStatus.DONE.name());
      signals.put("TRUST", CheckStatus.DONE.name());

      String keyJson = toStableJson(signals);
      String keyHash = sha256Hex(keyJson);

      UserPreProfileKey pk = profileKeyRepo.findByKeyHash(keyHash).orElseGet(() ->
          profileKeyRepo.save(
              UserPreProfileKey.builder()
                  .keyHash(keyHash)
                  .keyJson(keyJson)
                  .createdAt(LocalDateTime.now())
                  .build()
          )
      );

      String postGroupCode = route(signals); // => POST_A

      Long postTemplateId = postTemplateRepo
          .findTopByPhaseAndPostGroupCodeAndStatusOrderByVersionNoDesc(
              Phase.POST, postGroupCode, TemplateStatus.ACTIVE
          )
          .orElseThrow(() -> new IllegalArgumentException("ACTIVE POST 템플릿 없음: " + postGroupCode))
          .getTemplateId();

      ChecklistSession postSession = ChecklistSession.builder()
          .memberId(memberId)
          .phase(Phase.POST)
          .template(ChecklistTemplate.builder().templateId(postTemplateId).build())
          .profileKeyId(pk.getProfileKeyId()) // ✅ null 금지 -> 제약조건 통과
          .status("IN_PROGRESS")
          .startedAt(LocalDateTime.now())
          .deletedYn("N")
          .build();

      ChecklistSession saved = sessionRepo.save(postSession);

      return PostChecklistDTO.builder()
          .sessionId(saved.getSessionId())
          .templateId(postTemplateId)
          .postGroupCode(postGroupCode)
          .profileKeyId(pk.getProfileKeyId())
          .build();
    }

    // ✅ 3) 여기부터는 기존 PRE 기반 로직 그대로
    ChecklistSession preSession = preOpt.get();

    /* 2) PRE 템플릿 ID */
    Long preTemplateId = preSession.getTemplate().getTemplateId();

    /* 3) PRE 핵심 문항 조회 */
    List<ChecklistItem> signalItems =
        itemRepo.findByTemplate_TemplateIdAndItemOrderIn(preTemplateId, SIGNAL_ORDERS);

    Map<Integer, Long> orderToItemId = signalItems.stream()
        .collect(Collectors.toMap(ChecklistItem::getItemOrder, ChecklistItem::getItemId));

    /* 4) PRE 응답 조회 */
    List<ChecklistResponse> preResponses =
        responseRepo.findBySession_SessionId(preSession.getSessionId());

    Map<Long, String> itemIdToStatus = preResponses.stream()
        .collect(Collectors.toMap(r -> r.getItem().getItemId(),
                                  ChecklistResponse::getCheckStatus,
                                  (a, b) -> a));

    /* 5) signal 맵 구성 */
    Map<String, String> signals = new LinkedHashMap<>();
    signals.put("GAPGU_RISK", getStatus(orderToItemId, itemIdToStatus, 6));
    signals.put("EULGU_MORTGAGE", getStatus(orderToItemId, itemIdToStatus, 7));
    signals.put("PRIOR_SAFE", getStatus(orderToItemId, itemIdToStatus, 8));
    signals.put("BUILDING_ILLEGAL", getStatus(orderToItemId, itemIdToStatus, 12));
    signals.put("MOVEIN_OK", getStatus(orderToItemId, itemIdToStatus, 13));
    signals.put("TRUST", getStatus(orderToItemId, itemIdToStatus, 14));

    /* 6) profile key 생성 */
    String keyJson = toStableJson(signals);
    String keyHash = sha256Hex(keyJson);

    UserPreProfileKey pk = profileKeyRepo.findByKeyHash(keyHash).orElseGet(() ->
        profileKeyRepo.save(
            UserPreProfileKey.builder()
                .keyHash(keyHash)
                .keyJson(keyJson)
                .createdAt(LocalDateTime.now())
                .build()
        )
    );

    /* 7) POST 그룹 라우팅 */
    String postGroupCode = route(signals);

    /* 8) POST 템플릿 결정 */
    Long postTemplateId = assignmentRepo
        .findByProfileKeyIdAndPostGroupCodeAndActiveYn(pk.getProfileKeyId(), postGroupCode, "Y")
        .map(PostTemplateAssignment::getTemplateId)
        .orElseGet(() -> postTemplateRepo
            .findTopByPhaseAndPostGroupCodeAndStatusOrderByVersionNoDesc(
                Phase.POST, postGroupCode, TemplateStatus.ACTIVE
            )
            .orElseThrow(() -> new IllegalArgumentException("ACTIVE POST 템플릿 없음: " + postGroupCode))
            .getTemplateId()
        );

    /* 9) POST 세션 생성 */
    ChecklistSession postSession = ChecklistSession.builder()
        .memberId(memberId)
        .phase(Phase.POST)
        .template(ChecklistTemplate.builder().templateId(postTemplateId).build())
        .profileKeyId(pk.getProfileKeyId())
        .status("IN_PROGRESS")
        .startedAt(LocalDateTime.now())
        .deletedYn("N")
        .build();

    ChecklistSession saved = sessionRepo.save(postSession);

    /* 10) DTO 반환 */
    return PostChecklistDTO.builder()
        .sessionId(saved.getSessionId())
        .templateId(postTemplateId)
        .postGroupCode(postGroupCode)
        .profileKeyId(pk.getProfileKeyId())
        .build();
  }

  /* ===== helper ===== */

  private String getStatus(Map<Integer, Long> orderToItemId, Map<Long, String> itemIdToStatus, int order) {
    Long itemId = orderToItemId.get(order);
    if (itemId == null) return CheckStatus.NOT_DONE.name();
    return itemIdToStatus.getOrDefault(itemId, CheckStatus.NOT_DONE.name());
  }

  private String route(Map<String, String> s) {
    if (isNotDone(s.get("GAPGU_RISK")) || isNotDone(s.get("TRUST"))) return POST_D;
    if (isNotDone(s.get("EULGU_MORTGAGE")) || isNotDone(s.get("PRIOR_SAFE"))) return POST_C;
    if (isNotDone(s.get("BUILDING_ILLEGAL")) || isNotDone(s.get("MOVEIN_OK"))) return POST_B;
    return POST_A;
  }

  private boolean isNotDone(String st) {
    return st == null || CheckStatus.NOT_DONE.name().equals(st);
  }

  private String toStableJson(Map<String, String> signals) {
    String body = signals.entrySet().stream()
        .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
        .collect(Collectors.joining(","));
    return "{\"version\":1,\"signals\":{" + body + "}}";
  }

  private String sha256Hex(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Transactional(readOnly = true)
  public PostChecklistResponseDTO getPostChecklist(Long sessionId) {

    ChecklistSession session = sessionRepo.findBySessionIdAndDeletedYn(sessionId, "N")
        .orElseThrow(() -> new IllegalArgumentException("POST 세션 없음"));

    ChecklistTemplate template = session.getTemplate();

    List<ChecklistItem> items = itemRepo.findPostItemsOrdered(template.getTemplateId());

    List<PostChecklistItemDTO> itemDTOs = items.stream()
        .map(it -> PostChecklistItemDTO.builder()
            .itemId(it.getItemId())
            .itemOrder(it.getItemOrder())
            .checkArea(it.getCheckArea())
            .title(it.getTitle())
            .description(it.getDescription())
            .requiredYn(it.getRequiredYn())
            .build())
        .toList();

    return PostChecklistResponseDTO.builder()
        .sessionId(sessionId)
        .templateId(template.getTemplateId())
        .templateName(template.getTemplateName())
        .items(itemDTOs)
        .build();
  }

  public List<PostChecklistStatusDTO> getPostStatuses(Long sessionId) {
    List<ChecklistResponse> responses = responseRepo.findBySession_SessionId(sessionId);

    return responses.stream()
        .map(r -> PostChecklistStatusDTO.builder()
            .itemId(r.getItem().getItemId())
            .checkStatus(r.getCheckStatus())
            .build())
        .toList();
  }

  @Transactional
  public void updateCheckStatus(Long sessionId, Long itemId, String checkStatus) {

    if (checkStatus == null) throw new IllegalArgumentException("checkStatus가 없습니다.");
    if (!checkStatus.equals("DONE") && !checkStatus.equals("NOT_DONE") && !checkStatus.equals("NOT_REQUIRED")) {
      throw new IllegalArgumentException("허용되지 않은 checkStatus: " + checkStatus);
    }

    ChecklistResponse resp = responseRepo.findBySession_SessionIdAndItem_ItemId(sessionId, itemId)
        .orElseGet(() -> ChecklistResponse.builder()
            .session(ChecklistSession.builder().sessionId(sessionId).build())
            .item(ChecklistItem.builder().itemId(itemId).build())
            .build());

    resp.setCheckStatus(checkStatus);
    resp.setUpdatedAt(LocalDateTime.now());

    responseRepo.save(resp);
  }

  @Transactional
  public void forceCompletePostSession(Long sessionId) {
    int updated = sessionRepo.markCompleted(sessionId, LocalDateTime.now());
    if (updated == 0) {
      throw new IllegalArgumentException("완료 처리 실패(세션 없음/삭제됨): " + sessionId);
    }
  }

  @Transactional(readOnly = true)
  public Page<ChecklistHistoryRowDTO> getPostHistoryDto(Long memberId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);

    Page<ChecklistSession> p =
        sessionRepo.findByMemberIdAndPhaseAndDeletedYn(memberId, Phase.POST, "N", pageable);

    return p.map(s -> ChecklistHistoryRowDTO.builder()
        .sessionId(s.getSessionId())
        .phase(s.getPhase().name())
        .status(s.getStatus())
        .startedAt(s.getStartedAt())
        .completedAt(s.getCompletedAt())
        .build());
  }

  @Transactional
  public void deletePostSession(Long sessionId) {
    int updated = sessionRepo.softDeletePostSession(sessionId, LocalDateTime.now());

    if (updated == 0) {
      throw new IllegalArgumentException("삭제 실패: POST 세션이 없거나 이미 삭제됨");
    }
  }

  @Transactional(readOnly = true)
  public PostChecklistSummaryDTO getSummary(Long sessionId) {

    ChecklistSession session = sessionRepo.findBySessionIdAndDeletedYn(sessionId, "N")
        .orElseThrow(() -> new IllegalArgumentException("POST 세션 없음"));

    // 해당 세션의 아이템들
    List<ChecklistItem> items = itemRepo.findPostItemsOrdered(session.getTemplate().getTemplateId());
    if (items.isEmpty()) {
      return PostChecklistSummaryDTO.builder()
          .level("EMPTY")
          .message("표시할 체크리스트 항목이 없습니다. (템플릿/아이템 확인 필요)")
          .build();
    }

    // 응답 상태들
    List<ChecklistResponse> responses = responseRepo.findBySession_SessionId(sessionId);
    Map<Long, String> statusMap = responses.stream()
        .collect(Collectors.toMap(r -> r.getItem().getItemId(), ChecklistResponse::getCheckStatus, (a, b) -> a));

    long requiredTotal = items.stream().filter(i -> "Y".equals(i.getRequiredYn())).count();
    long requiredNotDone = items.stream()
        .filter(i -> "Y".equals(i.getRequiredYn()))
        .filter(i -> "NOT_DONE".equals(statusMap.getOrDefault(i.getItemId(), "NOT_DONE")))
        .count();

    String level;
    String message;

    if (requiredNotDone == 0) {
      level = "OK";
      message = "필수 항목이 모두 완료되었습니다. 완료 버튼을 눌러 마무리하세요.";
    } else if (requiredNotDone <= 2) {
      level = "WARN";
      message = "필수 항목이 " + requiredNotDone + "개 남아있습니다. 마저 체크해 주세요.";
    } else {
      level = "RISK";
      message = "필수 항목이 " + requiredNotDone + "개 남아있습니다. 중요한 항목부터 우선 확인하세요.";
    }

    return PostChecklistSummaryDTO.builder()
        .level(level)
        .message(message)
        .build();
  }
}
