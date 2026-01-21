package dev.jpa.team2.checklist.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.ai.ChecklistAiScoreClient;
import dev.jpa.team2.checklist.dto.PostStartResponse;
import dev.jpa.team2.checklist.enums.CheckStatus;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.SessionStatus;
import dev.jpa.team2.checklist.enums.TemplateStatus;
import dev.jpa.team2.checklist.enums.Yn;
import dev.jpa.team2.checklist.model.ChecklistItem;
import dev.jpa.team2.checklist.model.ChecklistResponse;
import dev.jpa.team2.checklist.model.ChecklistSatisfaction;
import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import dev.jpa.team2.checklist.model.PostDecisionLog;
import dev.jpa.team2.checklist.repository.ChecklistSatisfactionRepository;
import dev.jpa.team2.checklist.repository.ItemRepository;
import dev.jpa.team2.checklist.repository.PostDecisionLogRepository;
import dev.jpa.team2.checklist.repository.ResponseRepository;
import dev.jpa.team2.checklist.repository.SessionRepository;
import dev.jpa.team2.checklist.repository.TemplateItemRepository;
import dev.jpa.team2.checklist.repository.TemplateRepository;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreItem;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreRequest;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreResponse;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostChecklistSessionService {

  private final SessionRepository sessionRepository;
  private final TemplateRepository templateRepository;
  private final TemplateItemRepository templateItemRepository;
  private final ItemRepository itemRepository;
  private final ResponseRepository responseRepository;
  private final ChecklistSatisfactionRepository satisfactionRepository;
  private final ChecklistAiScoreClient checklistAiScoreClient;
  private final PostDecisionLogRepository postDecisionLogRepository;

  /**
   * POST 체크리스트 세션 시작
   */
  @Transactional
  public PostStartResponse startPostSession(Long memberId, Long preSessionId) {

    // 0️⃣ PRE 세션 결정
    ChecklistSession preSession;

    if (preSessionId == null) {
      preSession = sessionRepository
          .findTopByMemberIdAndPhaseAndStatusOrderBySessionIdDesc(memberId, ChecklistPhase.PRE, SessionStatus.COMPLETED)
          .orElseThrow(() -> new IllegalStateException("완료된 PRE 체크리스트가 없습니다."));
    } else {
      preSession = sessionRepository.findById(preSessionId)
          .orElseThrow(() -> new IllegalStateException("PRE 세션이 존재하지 않습니다."));
    }

    // 1️⃣ 소유자 검증
    if (!preSession.getMemberId().equals(memberId)) {
      throw new IllegalStateException("PRE 세션 소유자가 아닙니다.");
    }

    // 2️⃣ 완료 상태 검증
    if (preSession.getStatus() != SessionStatus.COMPLETED) {
      throw new IllegalStateException("PRE 체크리스트가 완료되지 않았습니다.");
    }

    /*
     * ================================================= 3️⃣ POST_GROUP_CODE 결정 (항상
     * 서버 판단) =================================================
     */
    PostDecisionResult decisionResult = resolvePostGroupCode(preSession);
    String postGroupCode = decisionResult.postGroupCode;

    log.info("[POST][DECISION] preSessionId={}, postGroupCode={}, riskScoreSum={}", preSession.getSessionId(),
        decisionResult.postGroupCode, decisionResult.riskScoreSum);

    // 4️⃣ 같은 PRE 세션에서 시작된 POST 진행중 세션 이어하기
    return sessionRepository
        .findByPreSessionIdAndPhaseAndStatus(preSession.getSessionId(), ChecklistPhase.POST, SessionStatus.IN_PROGRESS)
        .map(s -> {
          // 방어 로직
          if (!itemRepository.existsBySessionId(s.getSessionId())) {
            throw new IllegalStateException("POST 세션에 체크리스트 항목이 없습니다. 새로 시작하세요.");
          }
          return new PostStartResponse(s.getSessionId(), postGroupCode, s.getTemplateId());
        })
        // 5️⃣ 없으면 신규 POST 세션 생성
        .orElseGet(() -> createNewPostSession(memberId, postGroupCode, preSession, decisionResult));
  }

  /**
   * PRE 결과 기반 POST_GROUP_CODE 결정 - 모든 PRE 체크리스트 항목을 AI로 평가 - DONE / NOT_DONE 여부와
   * 관계없이 AI 점수 기준 분기
   */
  private PostDecisionResult resolvePostGroupCode(ChecklistSession preSession) {

    log.info("[POST][RESOLVE] start resolvePostGroupCode preSessionId={}", preSession.getSessionId());

    // 1️⃣ PRE 세션의 모든 응답 조회 (DONE / NOT_DONE 포함)
    List<ChecklistResponse> responses = responseRepository.findBySessionId(preSession.getSessionId());

    if (responses.isEmpty()) {
      // 응답 자체가 없으면 가장 안전한 POST_A
      return new PostDecisionResult("POST_A", 0.0, null);
    }

    // 2️⃣ 응답에 해당하는 모든 ITEM 조회
    List<Long> itemIds = responses.stream().map(ChecklistResponse::getItemId).distinct().toList();

    List<ChecklistItem> items = itemRepository.findBySessionIdAndItemIdIn(preSession.getSessionId(), itemIds);

    if (items.isEmpty()) {
      return new PostDecisionResult("POST_A", 0.0, null);
    }

    // 3️⃣ AI 점수 요청 DTO 생성 (⚠️ 전 항목 포함)
    ChecklistScoreRequest scoreRequest = new ChecklistScoreRequest();
    scoreRequest.setItems(items.stream().map(item -> {
      ChecklistScoreItem dto = new ChecklistScoreItem();
      dto.setItemId(item.getItemId());
      dto.setTitle(item.getTitle());
      dto.setDescription(item.getDescription());
      return dto;
    }).toList());

    // 4️⃣ 🔥 FastAPI AI 서버 호출 (항상 호출)
    ChecklistScoreResponse scoreResponse = checklistAiScoreClient.scoreChecklistItems(scoreRequest);

    if (scoreResponse == null || scoreResponse.getScores() == null) {

      log.warn("[POST][RESOLVE][AI_FAIL] AI score unavailable. fallback to POST_B. preSessionId={}",
          preSession.getSessionId());

      // AI 판단 불가 → 보수적 분기
      return new PostDecisionResult("POST_B", 0.0, null);
    }

    // 5️⃣ 점수 집계 + 고위험 항목 판별
    double riskScoreSum = 0.0;
    List<Long> highRiskItemIds = new java.util.ArrayList<>();

    for (ChecklistScoreResult score : scoreResponse.getScores()) {

      if (score.getImportanceScore() == null) {
        continue;
      }

      riskScoreSum += score.getImportanceScore();

      // ⚠️ 고위험 기준 (조정 가능)
      if (score.getImportanceScore() >= 0.8) {
        highRiskItemIds.add(score.getItemId());
      }
    }

    // 6️⃣ POST 분기 결정 (AI 기준 단일화)
    String postGroupCode = (!highRiskItemIds.isEmpty() || riskScoreSum >= 1.5) ? "POST_B" : "POST_A";

    log.info("[POST][RESOLVE][AI] groupCode={}, riskScoreSum={}, highRiskItemCount={}", postGroupCode, riskScoreSum,
        highRiskItemIds.size());

    return new PostDecisionResult(postGroupCode, riskScoreSum, highRiskItemIds.isEmpty() ? null
        : highRiskItemIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
  }

  /**
   * 신규 POST 세션 생성
   */
  private PostStartResponse createNewPostSession(Long memberId, String postGroupCode, ChecklistSession preSession,
      PostDecisionResult decisionResult) {

    // 🔍 [LOG-1] 메서드 진입 확인
    log.info("[POST][CREATE] start createNewPostSession memberId={}, postGroupCode={}", memberId, postGroupCode);

    // 1️⃣ POST 템플릿 조회
    ChecklistTemplate template = templateRepository
        .findFirstByPhaseAndStatusAndPostGroupCode(ChecklistPhase.POST, TemplateStatus.ACTIVE, postGroupCode)
        .orElseThrow(() -> new IllegalStateException("ACTIVE POST 템플릿이 없습니다. group=" + postGroupCode));

    // 🔍 [LOG-2] 템플릿 조회 성공
    log.info("[POST][CREATE] template found templateId={}, templateName={}", template.getTemplateId(),
        template.getTemplateName());

    // 2️⃣ 세션 생성
    ChecklistSession session = new ChecklistSession();
    session.setMemberId(memberId);
    session.setPhase(ChecklistPhase.POST);
    session.setStatus(SessionStatus.IN_PROGRESS);
    session.setTemplateId(template.getTemplateId());
    session.setPreSessionId(preSession.getSessionId());
    session.setDeletedYn(Yn.N);

    ChecklistSession saved = sessionRepository.save(session);

    // 🔍 [LOG-3] 세션 생성 확인
    log.info("[POST][CREATE] session created sessionId={}", saved.getSessionId());

    /*
     * ================================================= 🔥 POST 분기 판단 로그 저장 (여기!) -
     * session 저장 직후 - 같은 함수, 같은 트랜잭션
     * =================================================
     */
    if (decisionResult != null) {
      PostDecisionLog logEntity = new PostDecisionLog();
      logEntity.setPreSessionId(preSession.getSessionId());
      logEntity.setPostSessionId(saved.getSessionId());
      logEntity.setResultCode(decisionResult.postGroupCode);
      logEntity.setRiskScoreSum(
          decisionResult.riskScoreSum == null ? null : BigDecimal.valueOf(decisionResult.riskScoreSum));
      logEntity.setHighRiskItemIds(decisionResult.highRiskItemIds);
      logEntity.setDecisionReason("AI 중요도 점수 기반 POST 분기 판단");
      logEntity.setCreatedAt(new Date());

      postDecisionLogRepository.save(logEntity);
    }

    // 3️⃣ TEMPLATE_ITEM → CHECKLIST_ITEM 복제 (기존 로직 그대로)
    List<ChecklistTemplateItem> templateItems = templateItemRepository
        .findByTemplate_TemplateIdOrderByItemOrderAsc(template.getTemplateId());

    // 🔍 [LOG-4] 템플릿 아이템 개수 확인
    log.info("[POST][CREATE] templateItems count={}", templateItems.size());

    if (templateItems.isEmpty()) {
      throw new IllegalStateException("POST 템플릿에 항목이 없습니다.");
    }

    int copiedCount = 0;

    for (ChecklistTemplateItem ti : templateItems) {

      ChecklistItem item = new ChecklistItem();
      item.setSessionId(saved.getSessionId());
      item.setItemOrder(ti.getItemOrder());
      item.setRequiredYn(ti.getRequiredYn());
      item.setActiveYn(ti.getActiveYn());

      // MASTER → SESSION 복제
      item.setCheckArea(ti.getItemMaster().getCheckArea());
      item.setTitle(ti.getItemMaster().getTitle());
      item.setDescription(ti.getItemMaster().getDescription());

      itemRepository.save(item);
      copiedCount++;
    }

    // 🔍 [LOG-5] 아이템 복제 완료 확인
    log.info("[POST][CREATE] copied {} items into sessionId={}", copiedCount, saved.getSessionId());

    return new PostStartResponse(saved.getSessionId(), postGroupCode, template.getTemplateId());
  }

  /**
   * POST 체크리스트 항목 상태 변경
   */
  @Transactional
  public void updateItemStatus(Long sessionId, Long itemId, CheckStatus checkStatus) {

    ChecklistResponse response = responseRepository.findBySessionIdAndItemId(sessionId, itemId).orElseGet(() -> {
      ChecklistResponse r = new ChecklistResponse();
      r.setSessionId(sessionId);
      r.setItemId(itemId);
      return r;
    });

    response.setCheckStatus(checkStatus);
    response.setUpdatedAt(new Date());

    responseRepository.save(response);
  }

  /**
   * POST 체크리스트 세션 완료 처리
   */
  @Transactional
  public void completeSession(Long sessionId) {
    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("세션 없음"));

    session.setStatus(SessionStatus.COMPLETED);
    session.setCompletedAt(new Date());

    sessionRepository.save(session);
  }

  /**
   * POST 체크리스트 만족도 저장 - 세션당 1회만 저장
   */
  @Transactional
  public void saveSatisfaction(Long sessionId, Integer rating, String commentText) {

    // 1️⃣ 세션 존재 확인
    sessionRepository.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("세션이 존재하지 않습니다."));

    // 2️⃣ 이미 만족도 있으면 저장 금지
    if (satisfactionRepository.existsBySessionId(sessionId)) {
      return; // 프론트 중복 방지 흐름과 맞춤
    }

    // 3️⃣ 저장
    ChecklistSatisfaction entity = new ChecklistSatisfaction();
    entity.setSessionId(sessionId);
    entity.setRating(rating);
    entity.setCommentText(commentText);
    entity.setCreatedAt(new Date());

    satisfactionRepository.save(entity);
  }

  @Transactional
  public void deleteSession(Long sessionId) {

    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("세션 없음"));

    // ✅ POST 완료 후 즉시 삭제 방지 (예: 5분)
    if (session.getPhase() == ChecklistPhase.POST && session.getStatus() == SessionStatus.COMPLETED
        && session.getCompletedAt() != null) {

      long diff = System.currentTimeMillis() - session.getCompletedAt().getTime();

      if (diff < 5 * 60 * 1000) { // 5분
        throw new IllegalStateException("완료 직후에는 삭제할 수 없습니다. 잠시 후 다시 시도하세요.");
      }
    }

    session.setDeletedYn(Yn.Y);
    session.setDeletedAt(new Date());
  }

  /**
   * POST 분기 판단 결과 DTO (내부 전용)
   */
  private static class PostDecisionResult {

    private final String postGroupCode;
    private final Double riskScoreSum;
    private final String highRiskItemIds; // "12,15,18"

    private PostDecisionResult(String postGroupCode, Double riskScoreSum, String highRiskItemIds) {
      this.postGroupCode = postGroupCode;
      this.riskScoreSum = riskScoreSum;
      this.highRiskItemIds = highRiskItemIds;
    }
  }

}
