package dev.jpa.team2.checklist.admin.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.admin.dto.AdminPostDecisionDebugResponse;
import dev.jpa.team2.checklist.admin.dto.AdminPostDecisionScoreRowDto;
import dev.jpa.team2.checklist.ai.ChecklistAiScoreClient;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreItem;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreRequest;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreResponse;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreResult;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.SessionStatus;
import dev.jpa.team2.checklist.model.ChecklistItem;
import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.repository.ItemRepository;
import dev.jpa.team2.checklist.repository.SessionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminPostDecisionService {

    private final SessionRepository sessionRepository;
    private final ItemRepository itemRepository;
    private final ChecklistAiScoreClient checklistAiScoreClient;

    @Transactional(readOnly = true)
    public AdminPostDecisionDebugResponse debugPostDecision(Long preSessionId) {

        // =========================================================
        // 1️⃣ PRE 세션 검증
        // =========================================================
        ChecklistSession preSession = sessionRepository.findById(preSessionId)
            .orElseThrow(() -> new IllegalArgumentException("PRE 세션이 존재하지 않습니다."));

        if (preSession.getPhase() != ChecklistPhase.PRE ||
            preSession.getStatus() != SessionStatus.COMPLETED) {
            throw new IllegalStateException("완료된 PRE 세션만 조회할 수 있습니다.");
        }

        // =========================================================
        // 2️⃣ NOT_DONE 항목 조회
        // =========================================================
        List<ChecklistItem> notDoneItems =
            itemRepository.findNotDoneItemsByPreSessionId(preSessionId);

        if (notDoneItems.isEmpty()) {
            // 미이행 항목이 없으면 무조건 POST_A
            return new AdminPostDecisionDebugResponse(
                "POST_A",
                0.0,
                List.of()
            );
        }

        // =========================================================
        // 3️⃣ itemId → ChecklistItem 매핑
        // =========================================================
        Map<Long, ChecklistItem> itemMap =
            notDoneItems.stream().collect(Collectors.toMap(
                ChecklistItem::getItemId,
                it -> it
            ));

        // =========================================================
        // 4️⃣ AI 스코어 요청 DTO 생성
        // =========================================================
        ChecklistScoreRequest scoreRequest = new ChecklistScoreRequest();
        scoreRequest.setItems(
            notDoneItems.stream().map(it -> {
                ChecklistScoreItem dto = new ChecklistScoreItem();
                dto.setItemId(it.getItemId());
                dto.setTitle(it.getTitle());
                dto.setDescription(it.getDescription());
                return dto;
            }).toList()
        );

        // =========================================================
        // 5️⃣ FastAPI 호출 (🔥 실패 시 POST_B 고정)
        // =========================================================
        ChecklistScoreResponse scoreResponse = null;

        try {
            scoreResponse = checklistAiScoreClient.scoreChecklistItems(scoreRequest);
        } catch (Exception e) {
            // 🔥 AI 서버 장애, 타임아웃 등 모든 예외 처리
            // 🔥 운영 정책: AI 실패 시 보수적으로 POST_B 고정
            return new AdminPostDecisionDebugResponse(
                "POST_B",
                0.0,
                List.of()
            );
        }

        // 🔥 AI 응답 자체가 비정상인 경우도 POST_B
        if (scoreResponse == null || scoreResponse.getScores() == null) {
            return new AdminPostDecisionDebugResponse(
                "POST_B",
                0.0,
                List.of()
            );
        }

        List<ChecklistScoreResult> scores = scoreResponse.getScores();

        // =========================================================
        // 6️⃣ 위험 점수 합산
        // =========================================================
        double riskScoreSum = scores.stream()
            .map(ChecklistScoreResult::getImportanceScore)
            .filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .sum();

        // =========================================================
        // 7️⃣ 관리자 UI 표시용 Row 생성
        // =========================================================
        List<AdminPostDecisionScoreRowDto> rows =
            scores.stream().map(s -> {

                ChecklistItem item = itemMap.get(s.getItemId());

                return new AdminPostDecisionScoreRowDto(
                    s.getItemId(),
                    item != null ? item.getTitle() : "(알 수 없음)",
                    s.getImportanceScore()
                );
            }).toList();

        // =========================================================
        // 8️⃣ POST 분기 판단
        // =========================================================
        String postGroupCode = riskScoreSum >= 1.5 ? "POST_B" : "POST_A";

        // =========================================================
        // 9️⃣ 결과 반환
        // =========================================================
        return new AdminPostDecisionDebugResponse(
            postGroupCode,
            riskScoreSum,
            rows
        );
    }




}
