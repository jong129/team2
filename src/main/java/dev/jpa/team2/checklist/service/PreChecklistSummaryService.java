package dev.jpa.team2.checklist.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import dev.jpa.team2.checklist.dto.PreChecklistSummaryDto;
import dev.jpa.team2.checklist.enums.CheckStatus;
import dev.jpa.team2.checklist.model.ChecklistResponse;
import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import dev.jpa.team2.checklist.repository.ResponseRepository;
import dev.jpa.team2.checklist.repository.SessionRepository;
import dev.jpa.team2.checklist.repository.TemplateItemRepository;

@Service
@RequiredArgsConstructor
public class PreChecklistSummaryService {

  private final ResponseRepository responseRepository;
  private final SessionRepository sessionRepository;
  private final TemplateItemRepository templateItemRepository;

  /**
   * ✅ PRE 체크리스트 요약
   */
  public PreChecklistSummaryDto getSummary(Long sessionId) {

    // 1️⃣ 세션 조회 → templateId 확보
    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalStateException("세션이 존재하지 않습니다."));

    Long templateId = session.getTemplateId();

    // 2️⃣ 템플릿 전체 항목 (기준)
    List<ChecklistTemplateItem> templateItems =
        templateItemRepository.findByTemplate_TemplateIdOrderByItemOrderAsc(templateId);

    // 3️⃣ 세션 응답 (보조 데이터)
    List<ChecklistResponse> responses = responseRepository.findBySessionId(sessionId);

    // itemId → CheckStatus 맵
    Map<Long, CheckStatus> responseMap = new HashMap<>();
    for (ChecklistResponse r : responses) {
      responseMap.put(r.getItemId(), r.getCheckStatus());
    }

    int totalCount = templateItems.size();
    int doneCount = 0;
    int requiredNotDone = 0;

    // 4️⃣ 템플릿 기준으로 정확히 계산
    for (ChecklistTemplateItem ti : templateItems) {

      Long itemId = ti.getItemMaster().getItemMasterId();
      CheckStatus status = responseMap.get(itemId);

      if (status == CheckStatus.DONE) {
        doneCount++;
      }

      if ("Y".equals(ti.getRequiredYn()) && status != CheckStatus.DONE) {
        requiredNotDone++;
      }
    }

    // 5️⃣ 레벨 산정
    String level;
    String message;

    if (requiredNotDone > 0) {
      level = "주의";
      message = "필수 항목이 완료되지 않았습니다.";
    } else if (doneCount == totalCount && totalCount > 0) {
      level = "안전";
      message = "모든 점검 항목이 완료되었습니다.";
    } else {
      level = "보통";
      message = "일부 항목이 아직 진행되지 않았습니다.";
    }

    return new PreChecklistSummaryDto(totalCount, doneCount, requiredNotDone, level, message);
  }
}
