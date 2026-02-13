package dev.jpa.team2.checklist.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.ai.PostChecklistReviewAiClient;
import dev.jpa.team2.checklist.dto.PostChecklistDto;
import dev.jpa.team2.checklist.dto.PostChecklistHistoryRowDto;
import dev.jpa.team2.checklist.dto.PostChecklistItemDto;
import dev.jpa.team2.checklist.dto.PostChecklistReviewItemDto;
import dev.jpa.team2.checklist.dto.PostChecklistReviewResponse;
import dev.jpa.team2.checklist.dto.PostChecklistSatisfactionDto;
import dev.jpa.team2.checklist.dto.PostItemStatusDto;
import dev.jpa.team2.checklist.enums.CheckStatus;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.SessionStatus;
import dev.jpa.team2.checklist.model.ChecklistItem;
import dev.jpa.team2.checklist.model.ChecklistResponse;
import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.repository.ChecklistSatisfactionRepository;
import dev.jpa.team2.checklist.repository.ItemMasterRepository;
import dev.jpa.team2.checklist.repository.ItemRepository;
import dev.jpa.team2.checklist.repository.ResponseRepository;
import dev.jpa.team2.checklist.repository.SessionRepository;
import dev.jpa.team2.checklist.repository.TemplateItemRepository;
import dev.jpa.team2.checklist.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostChecklistQueryService {

  private final SessionRepository sessionRepository;
  private final ResponseRepository responseRepository;
  private final TemplateRepository templateRepository;
  private final ItemRepository itemRepository;
  private final ChecklistSatisfactionRepository satisfactionRepository;
  private final PostChecklistReviewAiClient postChecklistReviewAiClient;

  /**
   * ✅ POST 체크리스트 기록 조회
   */
  public Page<PostChecklistHistoryRowDto> getPostHistory(Long memberId, SessionStatus status, Date from, Date to,
      int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sessionId"));

    return sessionRepository.searchPreHistory(memberId, ChecklistPhase.POST, status, from, to, pageable).map(
        s -> new PostChecklistHistoryRowDto(s.getSessionId(), s.getStatus(), s.getStartedAt(), s.getCompletedAt()));
  }

  @Transactional(readOnly = true)
  public List<PostItemStatusDto> getPostStatuses(Long sessionId) {

    return responseRepository.findBySessionId(sessionId).stream().map(r -> {
      PostItemStatusDto dto = new PostItemStatusDto();
      dto.setItemId(r.getItemId());
      dto.setCheckStatus(r.getCheckStatus());
      return dto;
    }).toList();
  }

  @Transactional(readOnly = true)
  public PostChecklistDto getPostChecklist(Long sessionId) {

    // 1️⃣ 세션 조회
    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalStateException("POST 세션이 존재하지 않습니다."));

    // 2️⃣ 템플릿 조회 (이름용)
    ChecklistTemplate template = templateRepository.findById(session.getTemplateId())
        .orElseThrow(() -> new IllegalStateException("POST 템플릿이 존재하지 않습니다."));

    // 3️⃣ 🔑 세션에 복제된 CHECKLIST_ITEM 조회 (중요)
    List<ChecklistItem> sessionItems = itemRepository.findBySessionIdOrderByItemOrderAsc(sessionId);

    // 4️⃣ 프론트용 DTO 변환
    List<PostChecklistItemDto> items = sessionItems.stream().map(item -> new PostChecklistItemDto(item.getItemId(), // ✅
                                                                                                                    // CHECKLIST_ITEM.ITEM_ID
        item.getCheckArea(), item.getTitle(), item.getDescription(), item.getRequiredYn())).toList();

    // 5️⃣ 반환
    return new PostChecklistDto(session.getSessionId(), template.getTemplateId(), template.getTemplateName(), items);
  }

  /**
   * POST 체크리스트 만족도 조회
   */
  @Transactional(readOnly = true)
  public PostChecklistSatisfactionDto getSatisfaction(Long sessionId) {
    return satisfactionRepository.findBySessionId(sessionId).map(entity -> {
      PostChecklistSatisfactionDto dto = new PostChecklistSatisfactionDto();
      dto.setRating(entity.getRating());
      dto.setCommentText(entity.getCommentText());
      return dto;
    }).orElse(null);
  }

  /*
   * POST 체크리스트 AI 요약
   */
  @Transactional(readOnly = true)
  public PostChecklistReviewResponse reviewCurrentStatus(Long sessionId) {

    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("POST 세션이 존재하지 않습니다."));

    List<ChecklistItem> items = itemRepository.findBySessionIdOrderByItemOrderAsc(sessionId);

    List<ChecklistResponse> responses = responseRepository.findBySessionId(sessionId);

    int totalCount = items.size();
    int doneCount = (int) responses.stream().filter(r -> r.getCheckStatus() == CheckStatus.DONE).count();

    List<ChecklistResponse> notDoneResponses = responses.stream()
        .filter(r -> r.getCheckStatus() == CheckStatus.NOT_DONE).toList();

    // ✅ 미완료 없으면 AI 호출 안 함
    if (notDoneResponses.isEmpty()) {
      return new PostChecklistReviewResponse(totalCount, doneCount, 0, "모든 항목이 완료된 상태입니다.", List.of());
    }

    // ✅ FastAPI로 보낼 NOT_DONE 항목 구성
    List<Map<String, Object>> notDoneItems = notDoneResponses.stream().map(r -> {

      ChecklistItem item = items.stream().filter(i -> i.getItemId().equals(r.getItemId())).findFirst()
          .orElseThrow(() -> new IllegalStateException("ITEM 매칭 실패"));

      Map<String, Object> map = new java.util.HashMap<>();
      map.put("itemId", item.getItemId());
      map.put("title", item.getTitle());
      map.put("description", item.getDescription());

      return map;
    }).toList();

    // ✅ AI 서버 호출
    return postChecklistReviewAiClient.review(totalCount, doneCount, notDoneItems);
  }

}
