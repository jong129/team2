package dev.jpa.team2.checklist.pre;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.model.ChecklistItem;
import dev.jpa.team2.checklist.model.ChecklistResponse;
import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.Phase;
import dev.jpa.team2.checklist.model.TemplateStatus;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

/**
 * 사전 체크리스트(PRE) 비즈니스 로직 담당 서비스
 *
 * ✔ 어떤 템플릿을 보여줄지 결정 ✔ DB(Entity)를 조회 ✔ Entity → DTO로 변환
 */
@Service
@RequiredArgsConstructor
public class PreChecklistService {

  /** 체크리스트 템플릿 조회용 Repository */
  private final PreChecklistTemplateRepository templateRepo;

  /** 체크리스트 항목 조회용 Repository */
  private final PreChecklistItemRepository itemRepo;

  /** 체크리스트 세션 조회/저장용 Repository */
  private final PreChecklistSessionRepository sessionRepo;

  /** 체크리스트 응답(체크 상태) 조회/저장용 Repository */
  private final PreChecklistResponseRepository responseRepo;

  /**
   * 현재 사용 중인(ACTIVE) 사전 체크리스트 조회
   */
  public PreChecklistDTO.PreChecklistRes getActivePreChecklist() {

    ChecklistTemplate template = templateRepo
        .findFirstByPhaseAndStatusOrderByVersionNoDesc(Phase.PRE, TemplateStatus.ACTIVE)
        .orElseThrow(() -> new IllegalStateException("ACTIVE 상태의 사전 체크리스트 템플릿이 없습니다. 초기 데이터 확인 필요"));

    var items = itemRepo.findByTemplate_TemplateIdAndActiveYnOrderByItemOrderAsc(template.getTemplateId(), "Y").stream()
        .map(item -> PreChecklistDTO.ItemRes.builder().itemId(item.getItemId()).itemOrder(item.getItemOrder())
            .checkArea(item.getCheckArea()).title(item.getTitle()).description(item.getDescription()).build())
        .collect(Collectors.toList());

    return PreChecklistDTO.PreChecklistRes.builder().templateId(template.getTemplateId())
        .templateName(template.getTemplateName()).items(items).build();
  }

  // =========================================================
  // ✅ 정책 반영: "사전 체크 시작"은 무조건 새 체크리스트 생성
  // ✅ "이어서 하기"는 진행중 세션이 있을 때만 이어서
  // =========================================================

  /**
   * (B-START) 사전 체크리스트 "새로 시작" (무조건 새 세션 생성) - 정책: 사전 체크 시작 = 새 체크리스트 생성
   */
  @Transactional
  public PreChecklistDTO.SessionRes createNewSession(Long memberId) {

    ChecklistTemplate template = templateRepo
        .findFirstByPhaseAndStatusOrderByVersionNoDesc(Phase.PRE, TemplateStatus.ACTIVE)
        .orElseThrow(() -> new IllegalStateException("ACTIVE 상태의 사전 체크리스트 템플릿이 없습니다."));

    ChecklistSession newSession = ChecklistSession.builder()
        .memberId(memberId)
        .phase(Phase.PRE)
        .template(template)
        .status("IN_PROGRESS")
        .deletedYn("N")          // ✅ NOT NULL 컬럼 직접 세팅
        .build();

    ChecklistSession saved = sessionRepo.save(newSession);

    return PreChecklistDTO.SessionRes.builder().sessionId(saved.getSessionId()).templateId(template.getTemplateId())
        .status(saved.getStatus()).reused(false) // ✅ 새로 생성
        .hasProgress(false) // ✅ 새 세션이므로 진행데이터 없음
        .build();
  }

  /**
   * (B-CONTINUE) 사전 체크리스트 "이어서 하기" - 진행중(IN_PROGRESS) + 삭제아님(DELETED_YN='N') 세션이
   * 있으면 반환 - 없으면 "없음" 응답
   */
  public PreChecklistDTO.SessionRes continueSession(Long memberId) {

    var existing = sessionRepo.findFirstByMemberIdAndPhaseAndStatusAndDeletedYn(memberId, Phase.PRE, "IN_PROGRESS",
        "N");

    if (existing.isEmpty()) {
      return PreChecklistDTO.SessionRes.builder().sessionId(null).templateId(null).status("NONE").reused(false)
          .hasProgress(false).build();
    }

    ChecklistSession session = existing.get();

    boolean hasProgress = responseRepo.existsBySession_SessionId(session.getSessionId());

    return PreChecklistDTO.SessionRes.builder().sessionId(session.getSessionId())
        .templateId(session.getTemplate().getTemplateId()).status(session.getStatus()).reused(true)
        .hasProgress(hasProgress).build();
  }

  /**
   * (호환용) 기존 API를 계속 쓰는 프론트가 있을 수 있어서 남김 - 기존 startOrGetSession은 "이어하기" 성격으로만
   * 동작하도록 변경 - 새로 시작은 createNewSession()을 호출해야 함
   */
  public PreChecklistDTO.SessionRes startOrGetSession(Long memberId) {
    return continueSession(memberId);
  }

  /**
   * (C) 체크리스트 항목 체크 상태 저장 (upsert) - DONE / NOT_DONE / NOT_REQUIRED
   */
  public void updateItemStatus(Long sessionId, Long itemId, PreChecklistDTO.UpdateItemReq req) {

    ChecklistSession session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 sessionId=" + sessionId));

    ChecklistItem item = itemRepo.findById(itemId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 itemId=" + itemId));

    String status = req.getCheckStatus();
    if (!"DONE".equals(status) && !"NOT_DONE".equals(status) && !"NOT_REQUIRED".equals(status)) {
      throw new IllegalArgumentException("checkStatus 값이 올바르지 않습니다: " + status);
    }

    ChecklistResponse response = responseRepo.findBySession_SessionIdAndItem_ItemId(sessionId, itemId)
        .orElseGet(() -> ChecklistResponse.builder().session(session).item(item).build());

    response.setCheckStatus(status);
    responseRepo.save(response);
  }

  /**
   * (D) 세션 요약/경고 - 진행률 - 필수(required) 미완료 목록
   */
  public PreChecklistDTO.SummaryRes getSummary(Long sessionId) {

    ChecklistSession session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 sessionId=" + sessionId));

    Long templateId = session.getTemplate().getTemplateId();

    var items = itemRepo.findByTemplate_TemplateIdAndActiveYnOrderByItemOrderAsc(templateId, "Y");
    var responses = responseRepo.findBySession_SessionId(sessionId);

    java.util.Map<Long, String> statusMap = new java.util.HashMap<>();
    for (var r : responses) {
      statusMap.put(r.getItem().getItemId(), r.getCheckStatus());
    }

    int total = items.size();
    int done = 0;

    java.util.List<PreChecklistDTO.WarnItem> requiredNotDone = new java.util.ArrayList<>();

    for (var item : items) {
      String st = statusMap.get(item.getItemId()); // null이면 아직 체크 안함
      if ("DONE".equals(st))
        done++;

      if ("Y".equals(item.getRequiredYn()) && !"DONE".equals(st)) {
        requiredNotDone.add(PreChecklistDTO.WarnItem.builder().itemId(item.getItemId()).title(item.getTitle()).build());
      }
    }

    int requiredNotDoneCount = requiredNotDone.size();

    String level;
    String message;
    if (requiredNotDoneCount == 0) {
      level = "INFO";
      message = "계약 전 핵심 확인 사항을 모두 점검했습니다.";
    } else if (requiredNotDoneCount <= 2) {
      level = "WARN";
      message = "핵심 확인 사항이 일부 미완료입니다. 계약 전 확인을 권장합니다.";
    } else {
      level = "DANGER";
      message = "핵심 확인 사항 미완료가 많습니다. 계약 진행 전 필수 확인이 필요합니다.";
    }

    return PreChecklistDTO.SummaryRes.builder().totalCount(total).doneCount(done)
        .requiredNotDoneCount(requiredNotDoneCount).requiredNotDoneItems(requiredNotDone).level(level).message(message)
        .build();
  }

  /**
   * (E) 사전 체크리스트 세션 초기화 - 해당 세션의 모든 체크 상태를 NOT_DONE으로 되돌림
   */
  @Transactional
  public void resetSession(Long sessionId) {

    sessionRepo.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 sessionId=" + sessionId));

    responseRepo.resetAllToNotDone(sessionId);
  }

  /**
   * (F) 이어하기용: 세션의 항목별 체크 상태 목록 조회 - response 테이블에 저장된 값들을 itemId 기준으로 내려줌
   */
  public java.util.List<PreChecklistDTO.ItemStatusRes> getItemStatuses(Long sessionId) {

    sessionRepo.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 sessionId=" + sessionId));

    var responses = responseRepo.findBySession_SessionId(sessionId);

    return responses.stream().map(r -> PreChecklistDTO.ItemStatusRes.builder().itemId(r.getItem().getItemId())
        .checkStatus(r.getCheckStatus()).build()).collect(Collectors.toList());
  }

  // =========================================================
  // ✅ 기록보기 / 삭제 / 완료 처리
  // =========================================================

  /**
   * (H) 기록보기 + 검색 + 페이징
   * - page: 0부터
   * - size: 기본 5 (5개 초과 시 다음 페이지)
   */
  @Transactional(readOnly = true)
  public PageResponseDTO<PreChecklistDTO.SessionHistoryItem> getPreHistoryPage(
      Long memberId,
      String status,
      LocalDateTime from,
      LocalDateTime to,
      String dateType,
      int page,
      int size
  ) {
      Pageable pageable = PageRequest.of(
          page,
          size,
          Sort.by(Sort.Direction.DESC, "startedAt")
      );

      boolean useCompletedAt = "COMPLETED".equalsIgnoreCase(dateType);

      Specification<ChecklistSession> spec = (root, query, cb) -> {
          java.util.List<Predicate> p = new java.util.ArrayList<>();

          p.add(cb.equal(root.get("memberId"), memberId));
          p.add(cb.equal(root.get("phase"), Phase.PRE));
          p.add(cb.equal(root.get("deletedYn"), "N"));

          if (status != null && !status.isBlank()) {
              p.add(cb.equal(root.get("status"), status));
          }

          Path<LocalDateTime> target =
              useCompletedAt ? root.get("completedAt") : root.get("startedAt");

          if (useCompletedAt) {
              p.add(cb.isNotNull(root.get("completedAt")));
          }

          if (from != null) p.add(cb.greaterThanOrEqualTo(target, from));
          if (to != null) p.add(cb.lessThanOrEqualTo(target, to));

          return cb.and(p.toArray(new Predicate[0]));
      };

      Page<ChecklistSession> pageResult = sessionRepo.findAll(spec, pageable);

      Page<PreChecklistDTO.SessionHistoryItem> dtoPage =
          pageResult.map(s ->
              PreChecklistDTO.SessionHistoryItem.builder()
                  .sessionId(s.getSessionId())
                  .templateId(s.getTemplate().getTemplateId())
                  .templateName(s.getTemplate().getTemplateName())
                  .status(s.getStatus())
                  .startedAt(s.getStartedAt())
                  .completedAt(s.getCompletedAt())
                  .build()
          );

      return PageResponseDTO.of(dtoPage);
  }

  /**
   * (I) 소프트 삭제: 세션 삭제 처리 - 본인 세션만 삭제 가능 - DELETED_YN='Y', DELETED_AT=SYSDATE(=new
   * Date())
   */
  @Transactional
  public void softDeleteSession(Long memberId, Long sessionId) {

    ChecklistSession session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 sessionId=" + sessionId));

    if (!session.getMemberId().equals(memberId)) {
      throw new IllegalArgumentException("본인 세션만 삭제할 수 있습니다.");
    }

    if ("Y".equals(session.getDeletedYn())) {
      return; // 이미 삭제된 경우는 그냥 종료(원하면 예외로 바꿔도 됨)
    }

    session.setDeletedYn("Y");
    session.setDeletedAt(LocalDateTime.now());

    sessionRepo.save(session);
  }

  /**
   * (J) 완료 처리: 진행중 세션을 COMPLETED로 변경 - 본인 세션만 완료 가능 - 삭제된 세션은 완료 불가
   */
  @Transactional
  public void completeSession(Long memberId, Long sessionId) {

    ChecklistSession session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 sessionId=" + sessionId));

    if (!session.getMemberId().equals(memberId)) {
      throw new IllegalArgumentException("본인 세션만 완료 처리할 수 있습니다.");
    }

    if ("Y".equals(session.getDeletedYn())) {
      throw new IllegalArgumentException("삭제된 세션은 완료 처리할 수 없습니다.");
    }

    // 이미 완료면 그대로 종료(원하면 예외로)
    if ("COMPLETED".equals(session.getStatus())) {
      return;
    }

    session.setStatus("COMPLETED");
    session.setCompletedAt(LocalDateTime.now());

    sessionRepo.save(session);
  }
}
