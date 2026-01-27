package dev.jpa.team2.checklist.service;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dev.jpa.team2.checklist.ai.ChecklistAiScoreClient;
import dev.jpa.team2.checklist.ai.PreRiskExplanationAiService;
import dev.jpa.team2.checklist.ai.dto.AiRiskAnalysisResult;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreRequest;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreResponse;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreResult;
import dev.jpa.team2.checklist.dto.PostStartResponse;
import dev.jpa.team2.checklist.dto.PreChecklistResultResponse;
import dev.jpa.team2.checklist.dto.PreChecklistSessionDto;
import dev.jpa.team2.checklist.dto.PreChecklistSessionItemDto;
import dev.jpa.team2.checklist.dto.PreChecklistSyncRequest;
import dev.jpa.team2.checklist.dto.PreRiskExplanationDto;
import dev.jpa.team2.checklist.enums.CheckStatus;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.SessionStatus;
import dev.jpa.team2.checklist.enums.TemplateStatus;
import dev.jpa.team2.checklist.enums.Yn;
import dev.jpa.team2.checklist.model.ChecklistItem;
import dev.jpa.team2.checklist.model.ChecklistResponse;
import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import dev.jpa.team2.checklist.repository.ItemRepository;
import dev.jpa.team2.checklist.repository.ResponseRepository;
import dev.jpa.team2.checklist.repository.SessionRepository;
import dev.jpa.team2.checklist.repository.TemplateItemRepository;
import dev.jpa.team2.checklist.repository.TemplateRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreChecklistSessionService {

  private final SessionRepository sessionRepository;
  private final TemplateRepository templateRepository;
  private final TemplateItemRepository templateItemRepository;
  private final ItemRepository itemRepository;
  private final ResponseRepository responseRepository;

  private final PostChecklistSessionService postChecklistService;
  private final PreRiskExplanationAiService preRiskExplanationAiService;
  private final ChecklistAiScoreClient checklistAiScoreClient;

  /**
   * PRE 체크리스트 세션 시작
   */
  /**
   * PRE 체크리스트 세션 시작 - URL 기반 세션 전략 - 항상 신규 세션 생성
   */
  @Transactional
  public ChecklistSession startPreSession(Long memberId) {
    return createNewPreSession(memberId);
  }

  /*
   * 이어하기
   */
  @Transactional(readOnly = true)
  public List<ChecklistSession> findInProgressPreSessions(Long memberId) {
    return sessionRepository.findByMemberIdAndPhaseAndStatus(memberId, ChecklistPhase.PRE, SessionStatus.IN_PROGRESS);
  }

  /**
   * 신규 PRE 세션 생성 - 모든 항목을 기본 NOT_DONE 상태로 선생성 - 프론트 기본 상태(NOT_DONE)와 DB 상태를 일치시킨다
   */
  private ChecklistSession createNewPreSession(Long memberId) {

    // 1️⃣ PRE 템플릿 조회 (최신 버전 1개)
    ChecklistTemplate template = templateRepository.findFirstByPhaseOrderByVersionNoDesc(ChecklistPhase.PRE)
        .orElseThrow(() -> new IllegalStateException("PRE 템플릿이 존재하지 않습니다."));

    // 2️⃣ ACTIVE 상태 검증
    if (template.getStatus() != TemplateStatus.ACTIVE) {
      throw new IllegalStateException("사전 체크리스트가 현재 초안 상태입니다. 관리자에게 문의하세요.");
    }

    // 3️⃣ PRE 세션 생성
    ChecklistSession session = new ChecklistSession();
    session.setMemberId(memberId);
    session.setPhase(ChecklistPhase.PRE);
    session.setStatus(SessionStatus.IN_PROGRESS);
    session.setTemplateId(template.getTemplateId());
    session.setDeletedYn(Yn.N);

    ChecklistSession savedSession = sessionRepository.save(session);

    // 4️⃣ 템플릿 항목 → 세션 항목 복사 + Response 선생성
    List<ChecklistTemplateItem> templateItems = templateItemRepository
        .findByTemplate_TemplateIdOrderByItemOrderAsc(template.getTemplateId());

    for (ChecklistTemplateItem ti : templateItems) {

      // 4-1️⃣ ChecklistItem 생성
      ChecklistItem item = new ChecklistItem();
      item.setSessionId(savedSession.getSessionId());
      item.setItemOrder(ti.getItemOrder());
      item.setRequiredYn(ti.getRequiredYn());
      item.setActiveYn(ti.getActiveYn());
      item.setTitle(ti.getItemMaster().getTitle());
      item.setDescription(ti.getItemMaster().getDescription());

      ChecklistItem savedItem = itemRepository.save(item);

      // 4-2️⃣ ChecklistResponse 기본 상태 NOT_DONE 선생성 (⭐ 핵심)
      ChecklistResponse response = new ChecklistResponse();
      response.setSessionId(savedSession.getSessionId());
      response.setItemId(savedItem.getItemId());

      // ✔ 프론트 기본 선택 상태와 동기화
      // ✔ "아무 것도 안 했다" = "미이행"
      response.setCheckStatus(CheckStatus.NOT_DONE);

      response.setUpdatedAt(new Date());
      responseRepository.save(response);
    }

    return savedSession;
  }

  /**
   * 체크리스트 항목 상태 저장
   */
  @Transactional
  public void saveItemStatus(Long sessionId, Long itemId, String checkStatus) {

    CheckStatus status = CheckStatus.valueOf(checkStatus);

    ChecklistResponse response = responseRepository.findBySessionIdAndItemId(sessionId, itemId).orElseGet(() -> {
      ChecklistResponse r = new ChecklistResponse();
      r.setSessionId(sessionId);
      r.setItemId(itemId);
      return r;
    });

    response.setCheckStatus(status);
    response.setUpdatedAt(new Date());

    responseRepository.save(response);
  }

  /**
   * PRE 체크리스트 완료 처리
   */
  @Transactional
  public PostStartResponse completePreSession(Long sessionId) {

    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalStateException("세션이 존재하지 않습니다."));

    if (session.getStatus() == SessionStatus.COMPLETED) {
      throw new IllegalStateException("이미 완료된 세션입니다.");
    }

    // 1️⃣ PRE 세션 완료 처리
    session.setStatus(SessionStatus.COMPLETED);
    session.setCompletedAt(new Date());

    // 2️⃣ POST 세션 시작 요청 (분기 판단은 POST 서비스 책임)
    return postChecklistService.startPostSession(session.getMemberId(), session.getSessionId());
  }

  /**
   * PRE 세션 리셋 - 모든 항목 상태를 NOT_DONE으로 초기화 - Response 삭제 ❌
   */
  @Transactional
  public void resetPreSession(Long sessionId) {

    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalStateException("세션이 존재하지 않습니다."));

    // 1️⃣ 모든 항목 상태를 NOT_DONE으로 되돌림
    responseRepository.updateStatusBySessionId(sessionId, CheckStatus.NOT_DONE);

    // 2️⃣ 세션 상태 초기화
    session.setStatus(SessionStatus.IN_PROGRESS);
    session.setCompletedAt(null);

    sessionRepository.save(session);
  }

  /**
   * PRE 세션 논리 삭제
   */
  @Transactional
  public void deletePreSession(Long sessionId, Long memberId) {

    ChecklistSession session = sessionRepository.findBySessionIdAndMemberId(sessionId, memberId)
        .orElseThrow(() -> new IllegalStateException("세션이 존재하지 않거나 삭제 권한이 없습니다."));

    if (session.getDeletedYn() == Yn.Y) {
      throw new IllegalStateException("이미 삭제된 세션입니다.");
    }

    session.setDeletedYn(Yn.Y);
    session.setDeletedAt(new Date());

    sessionRepository.save(session);
  }

  @Transactional
  public ChecklistSession startNewPreSession(Long memberId) {
    return createNewPreSession(memberId);
  }

  @Transactional(readOnly = true)
  public PreChecklistSessionDto getPreSession(Long sessionId) {

    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalStateException("세션 없음"));

    List<ChecklistItem> items = itemRepository.findBySessionIdOrderByItemOrderAsc(sessionId);

    List<ChecklistResponse> responses = responseRepository.findBySessionId(sessionId);

    Map<Long, CheckStatus> statusMap = responses.stream().collect(
        Collectors.toMap(ChecklistResponse::getItemId, ChecklistResponse::getCheckStatus, (oldVal, newVal) -> newVal));

    return new PreChecklistSessionDto(session.getSessionId(), session.getTemplateId(),
        items.stream()
            .map(it -> new PreChecklistSessionItemDto(it.getItemId(), it.getCheckArea(), it.getTitle(),
                it.getDescription(), it.getRequiredYn(), statusMap.getOrDefault(it.getItemId(), CheckStatus.NOT_DONE)))
            .toList());
  }

  @Transactional
  public void syncSession(Long sessionId, PreChecklistSyncRequest request) {
    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("세션 없음"));

    // 1️⃣ 기존 응답 전부 조회
    List<ChecklistResponse> responses = responseRepository.findBySessionId(sessionId);

    Map<Long, ChecklistResponse> responseMap = responses.stream().collect(Collectors.toMap(ChecklistResponse::getItemId, // ⭐
                                                                                                                         // 핵심
                                                                                                                         // 수정
        r -> r));

    // 2️⃣ 프론트에서 넘어온 상태로 덮어쓰기
    for (PreChecklistSyncRequest.ItemSyncDto dto : request.getItems()) {
      ChecklistResponse resp = responseMap.get(dto.getItemId());
      if (resp == null)
        continue;

      resp.setCheckStatus(dto.getCheckStatus());
      resp.setUpdatedAt(new Date());
    }

    // 3️⃣ 트랜잭션 종료 시 자동 flush
  }

  @Transactional(readOnly = true)
  public PreChecklistResultResponse getPreChecklistResult(Long sessionId) {

    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("PRE 세션이 존재하지 않습니다."));

    if (session.getPhase() != ChecklistPhase.PRE || session.getStatus() != SessionStatus.COMPLETED) {
      throw new IllegalStateException("완료된 PRE 세션만 결과를 조회할 수 있습니다.");
    }

    // 1️⃣ NOT_DONE 항목 조회
    List<ChecklistResponse> notDoneResponses = responseRepository.findBySessionIdAndCheckStatus(sessionId,
        CheckStatus.NOT_DONE);

    // 2️⃣ AI 위험 분석
    AiRiskAnalysisResult aiResult = analyzeRiskWithAi(notDoneResponses);

    double riskScoreSum = aiResult.getTotalScore();
    List<String> aiReasons = aiResult.getReasons();
    List<ChecklistScoreResult> detailItems = aiResult.getAllResults();

    // 3️⃣ POST 분기
    String postGroupCode = riskScoreSum >= 70 ? "POST_B" : "POST_A";

    // 4️⃣ 응답 DTO
    PreChecklistResultResponse response = new PreChecklistResultResponse();
    response.setPostGroupCode(postGroupCode);
    response.setRiskScoreSum(riskScoreSum);
    response.setMessage("사전 체크리스트 결과입니다.");

    // 요약 카드
    try {
      response.setRiskExplanation(preRiskExplanationAiService.generateExplanation(riskScoreSum, aiReasons));
    } catch (Exception e) {
      log.warn("PRE 위험 설명 LLM 실패, 기본 설명으로 대체", e);
      response.setRiskExplanation(buildDefaultRiskExplanation(riskScoreSum, List.of()));
    }

    // 상세보기 데이터
    response.setHighRiskItemIds(detailItems.stream().map(r -> String.valueOf(r.getItemId())).toList());
    response.setRiskAnalysisItems(detailItems);

    return response;
  }

  private PreRiskExplanationDto buildDefaultRiskExplanation(Double riskScoreSum, List<String> notDoneItemTitles) {
    PreRiskExplanationDto dto = new PreRiskExplanationDto();

    dto.setSummary(riskScoreSum >= 70 ? "현재 계약에는 보증금 반환에 영향을 줄 수 있는 위험 요소가 있습니다." : "일부 확인이 필요한 사항이 남아 있습니다.");

    dto.setReasons(notDoneItemTitles.stream().map(title -> "확인되지 않은 항목: " + title).toList());

    dto.setActions(List.of("계약 체결 전 관련 항목을 다시 한 번 확인하시기를 권장드립니다."));

    return dto;
  }

  /**
   * AI 위험 분석 실행 (1회 호출) - 중요도 점수 + reason + title 포함 - 요약용은 itemId 기준 중복 제거 후 상위
   * 3개
   */
  private AiRiskAnalysisResult analyzeRiskWithAi(List<ChecklistResponse> notDoneResponses) {

    if (notDoneResponses == null || notDoneResponses.isEmpty()) {
      return new AiRiskAnalysisResult(0.0, List.of(), List.of());
    }

    try {
      // 1️⃣ itemId 목록
      List<Long> itemIds = notDoneResponses.stream().map(ChecklistResponse::getItemId).toList();

      // 2️⃣ ChecklistItem 일괄 조회 (title 확보)
      Map<Long, ChecklistItem> itemMap = itemRepository.findAllById(itemIds).stream()
          .collect(Collectors.toMap(ChecklistItem::getItemId, item -> item));

      // 3️⃣ AI 요청 DTO 생성
      ChecklistScoreRequest request = ChecklistScoreRequest.from(notDoneResponses, itemMap);

      // 4️⃣ AI 서버 호출 (⭐ 단 1회)
      ChecklistScoreResponse aiResponse = checklistAiScoreClient.scoreChecklistItems(request);

      if (aiResponse != null && aiResponse.getScores() != null) {

        // 5️⃣ AI 결과 + DB title 병합
        List<ChecklistScoreResult> allResults = aiResponse.getScores().stream().map(r -> {

          ChecklistScoreResult dto = new ChecklistScoreResult();
          dto.setItemId(r.getItemId());
          dto.setImportanceScore(r.getImportanceScore());
          dto.setReason(r.getReason());

          ChecklistItem item = itemMap.get(r.getItemId());
          dto.setTitle(item != null ? item.getTitle() : "알 수 없는 항목");

          return dto;
        }).toList();

        // 6️⃣ 전체 위험 점수
        double totalScore = allResults.stream().map(ChecklistScoreResult::getImportanceScore).filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue).sum();

        // 7️⃣ 중요도 기준 정렬
        List<ChecklistScoreResult> sorted = allResults.stream().filter(r -> r.getImportanceScore() != null)
            .sorted(Comparator.comparing(ChecklistScoreResult::getImportanceScore).reversed()).toList();

        // 8️⃣ itemId 기준 중복 제거 → 상위 3개
        List<String> reasons = sorted.stream()
            .collect(Collectors.toMap(ChecklistScoreResult::getItemId, r -> r, (a, b) -> a, LinkedHashMap::new))
            .values().stream().limit(3).map(ChecklistScoreResult::getReason).filter(r -> r != null && !r.isBlank())
            .toList();

        return new AiRiskAnalysisResult(totalScore, reasons, // ⭐ 요약용 (서로 다른 항목 3개)
            allResults // ⭐ 상세보기용 (전체)
        );
      }

    } catch (Exception e) {
      log.warn("AI 위험 분석 실패, fallback 사용", e);
    }

    // fallback
    return new AiRiskAnalysisResult(notDoneResponses.size() * 10.0, List.of(), List.of());
  }

}
