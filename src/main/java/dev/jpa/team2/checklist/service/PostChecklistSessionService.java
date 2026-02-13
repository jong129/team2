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
  private final PostChecklistSummaryService postChecklistSummaryService; 

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
   * PRE 결과 기반 POST_GROUP_CODE 결정
   *
   * 분기 규칙: 1️⃣ NOT_DONE 항목이 0개면 → 무조건 POST_A 2️⃣ NOT_DONE 항목만 AI 중요도 평가 3️⃣
   * NOT_DONE 중 importanceScore >= 80 하나라도 있으면 → POST_B 4️⃣ 그 외 → POST_A
   */
  private PostDecisionResult resolvePostGroupCode(ChecklistSession preSession) {

    log.info("[POST][RESOLVE] start resolvePostGroupCode preSessionId={}", preSession.getSessionId());

    // =========================================================
    // 1️⃣ NOT_DONE 응답만 조회
    // =========================================================
    List<ChecklistResponse> notDoneResponses = responseRepository
        .findBySessionIdAndCheckStatus(preSession.getSessionId(), CheckStatus.NOT_DONE);

    // ✅ NOT_DONE이 하나도 없으면 무조건 POST_A
    if (notDoneResponses.isEmpty()) {
      log.info("[POST][RESOLVE] no NOT_DONE items → POST_A. preSessionId={}", preSession.getSessionId());

      return new PostDecisionResult("POST_A", 0.0, null);
    }

    // =========================================================
    // 2️⃣ NOT_DONE 항목에 해당하는 ITEM 조회
    // =========================================================
    List<Long> itemIds = notDoneResponses.stream().map(ChecklistResponse::getItemId).distinct().toList();

    List<ChecklistItem> items = itemRepository.findBySessionIdAndItemIdIn(preSession.getSessionId(), itemIds);

    if (items.isEmpty()) {
      // 방어 로직: 이 경우도 안전하게 POST_A
      log.warn("[POST][RESOLVE] NOT_DONE exists but items empty → POST_A. preSessionId={}", preSession.getSessionId());

      return new PostDecisionResult("POST_A", 0.0, null);
    }

    // =========================================================
    // 3️⃣ AI 스코어 요청 (NOT_DONE 항목만)
    // =========================================================
    ChecklistScoreRequest scoreRequest = new ChecklistScoreRequest();
    scoreRequest.setItems(items.stream().map(item -> {
      ChecklistScoreItem dto = new ChecklistScoreItem();
      dto.setItemId(item.getItemId());
      dto.setTitle(item.getTitle());
      dto.setDescription(item.getDescription());
      return dto;
    }).toList());

    ChecklistScoreResponse scoreResponse;

    try {
      scoreResponse = checklistAiScoreClient.scoreChecklistItems(scoreRequest);
    } catch (Exception e) {
      // 🔥 AI 장애 시 보수적으로 POST_B
      log.warn("[POST][RESOLVE][AI_FAIL] AI error → POST_B. preSessionId={}", preSession.getSessionId(), e);

      return new PostDecisionResult("POST_B", 0.0, null);
    }

    if (scoreResponse == null || scoreResponse.getScores() == null) {
      log.warn("[POST][RESOLVE][AI_NULL] AI response invalid → POST_B. preSessionId={}", preSession.getSessionId());

      return new PostDecisionResult("POST_B", 0.0, null);
    }

    // =========================================================
    // 4️⃣ 고위험 항목 판단 (NOT_DONE 기준)
    // =========================================================
    boolean hasHighRisk = false;
    double riskScoreSum = 0.0;

    for (ChecklistScoreResult score : scoreResponse.getScores()) {

      if (score.getImportanceScore() == null) {
        continue;
      }

      int importanceScore = score.getImportanceScore(); // 0~100

      riskScoreSum += importanceScore;

      // ⭐ 고위험 기준 (정책 기준)
      if (importanceScore >= 80) {
        hasHighRisk = true;
      }
    }

    // =========================================================
    // 5️⃣ POST 분기 결정
    // =========================================================
    String postGroupCode = hasHighRisk ? "POST_B" : "POST_A";

    log.info("[POST][RESOLVE][RESULT] postGroupCode={}, hasHighRisk={}, notDoneCount={}, riskScoreSum={}",
        postGroupCode, hasHighRisk, notDoneResponses.size(), riskScoreSum);

    return new PostDecisionResult(postGroupCode, riskScoreSum, null // 필요 시 고위험 itemId 목록 확장 가능
    );
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

    // =================================================
    // 🔥 POST CHECKLIST_RESPONSE 초기화 (중요)
    // - 모든 항목을 NOT_DONE으로 미리 생성
    // =================================================
    List<ChecklistItem> sessionItems = itemRepository.findBySessionIdOrderByItemOrderAsc(saved.getSessionId());

    for (ChecklistItem item : sessionItems) {

      ChecklistResponse response = new ChecklistResponse();
      response.setSessionId(saved.getSessionId());
      response.setItemId(item.getItemId());
      response.setCheckStatus(CheckStatus.NOT_DONE);
      response.setUpdatedAt(new Date());

      responseRepository.save(response);
    }

    log.info("[POST][CREATE] initialized {} checklist responses (NOT_DONE)", sessionItems.size());

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

    if (session.getStatus() == SessionStatus.COMPLETED) {
      return;
    }

    boolean existsNotDone = responseRepository.existsBySessionIdAndCheckStatus(sessionId, CheckStatus.NOT_DONE);

    if (existsNotDone) {
      throw new IllegalStateException("미완료 항목이 존재하여 완료할 수 없습니다.");
    }

    // ✅ 1️⃣ 상태 완료
    session.setStatus(SessionStatus.COMPLETED);
    session.setCompletedAt(new Date());

    // ✅ 2️⃣ AI 요약 생성 + 저장
    postChecklistSummaryService.generateAndSave(sessionId);
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
