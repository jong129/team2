package dev.jpa.team2.checklist.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jpa.team2.checklist.ai.PostChecklistReviewAiClient;
import dev.jpa.team2.checklist.dto.PostChecklistReviewResponse;
import dev.jpa.team2.checklist.dto.PostChecklistSummaryDto;
import dev.jpa.team2.checklist.enums.CheckStatus;
import dev.jpa.team2.checklist.model.ChecklistItem;
import dev.jpa.team2.checklist.model.ChecklistResponse;
import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.model.PostChecklistSummary;
import dev.jpa.team2.checklist.repository.ItemRepository;
import dev.jpa.team2.checklist.repository.PostChecklistSummaryRepository;
import dev.jpa.team2.checklist.repository.ResponseRepository;
import dev.jpa.team2.checklist.repository.SessionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostChecklistSummaryService {

  private final SessionRepository sessionRepository;
  private final ItemRepository itemRepository;
  private final ResponseRepository responseRepository;
  private final PostChecklistReviewAiClient reviewAiClient;
  private final PostChecklistSummaryRepository summaryRepository;

  /**
   * ✅ POST 체크리스트 완료 시 1회 실행 - 완료(DONE) + 해당없음(NOT_REQUIRED) 기준 요약 -
   * 미완료(NOT_DONE)는 완료 시점에 존재하지 않음
   */
  @Transactional
  public void generateAndSave(Long sessionId) {

    // 0️⃣ 이미 요약 있으면 중복 생성 방지
    if (summaryRepository.existsBySessionId(sessionId)) {
      return;
    }

    // 1️⃣ 세션 조회
    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("세션 없음"));

    // 2️⃣ 체크리스트 항목 + 응답 조회
    List<ChecklistItem> items = itemRepository.findBySessionIdOrderByItemOrderAsc(sessionId);
    List<ChecklistResponse> responses = responseRepository.findBySessionId(sessionId);

    int total = items.size();
    int done = (int) responses.stream()
        .filter(r -> r.getCheckStatus() == CheckStatus.DONE || r.getCheckStatus() == CheckStatus.NOT_REQUIRED).count();

    // 3️⃣ 완료된 항목 구성 (FastAPI 계약 맞춤)
    Map<Long, ChecklistItem> itemMap = items.stream().collect(Collectors.toMap(ChecklistItem::getItemId, i -> i));

    List<Map<String, Object>> completedItems = responses.stream()
        .filter(r -> r.getCheckStatus() == CheckStatus.DONE || r.getCheckStatus() == CheckStatus.NOT_REQUIRED)
        .map(r -> {
          ChecklistItem item = itemMap.get(r.getItemId());

          Map<String, Object> map = new java.util.HashMap<>();
          map.put("itemId", item.getItemId());
          map.put("title", item.getTitle());
          map.put("description", item.getDescription());
          map.put("status", r.getCheckStatus().name()); // DONE / NOT_REQUIRED
          return map;
        }).toList();

    // 4️⃣ FastAPI - POST 완료 요약 호출
    PostChecklistSummaryDto aiResult = reviewAiClient.summarize(total, done, completedItems);

    // 5️⃣ 엔티티 생성 및 저장
    PostChecklistSummary entity = new PostChecklistSummary();
    entity.setSessionId(sessionId);
    entity.setSummaryText(aiResult.getSummary());
    entity.setCreatedAt(new Date());

    // guides → JSON 문자열로 저장
    if (aiResult.getGuides() != null && !aiResult.getGuides().isEmpty()) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        entity.setGuidesJson(mapper.writeValueAsString(aiResult.getGuides()));
      } catch (Exception e) {
        // 가이드는 부가 정보 → 실패해도 요약은 저장
        entity.setGuidesJson(null);
      }
    }

    summaryRepository.save(entity);
  }

}
