package dev.jpa.team2.checklist.pre;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.jpa.team2.checklist.model.ChecklistItem;
import dev.jpa.team2.checklist.model.ChecklistResponse;
import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.Phase;
import dev.jpa.team2.checklist.model.TemplateStatus;
import lombok.RequiredArgsConstructor;

/**
 * 사전 체크리스트(PRE) 비즈니스 로직 담당 서비스
 *
 * ✔ 어떤 템플릿을 보여줄지 결정
 * ✔ DB(Entity)를 조회
 * ✔ Entity → DTO로 변환
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
     *
     * @return 사전 체크리스트 응답 DTO
     */
    public PreChecklistDTO.PreChecklistRes getActivePreChecklist() {

        // 1️. 사전(PRE) + 사용중(ACTIVE) 템플릿 1개 조회
        ChecklistTemplate template = templateRepo
                .findFirstByPhaseAndStatusOrderByVersionNoDesc(
                        Phase.PRE,
                        TemplateStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ACTIVE 상태의 사전 체크리스트 템플릿이 없습니다. 초기 데이터 확인 필요"
                        )
                );

        // 2️. 해당 템플릿에 속한 체크 항목들을 순서대로 조회
        var items = itemRepo
            .findByTemplate_TemplateIdAndActiveYnOrderByItemOrderAsc(template.getTemplateId(), "Y")
            .stream()
            .map(item -> PreChecklistDTO.ItemRes.builder()
                    .itemId(item.getItemId())
                    .itemOrder(item.getItemOrder())
                    .checkArea(item.getCheckArea())
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .build())
            .collect(Collectors.toList());



        // 3. 최종 응답 DTO 생성
        return PreChecklistDTO.PreChecklistRes.builder()
                .templateId(template.getTemplateId())
                .templateName(template.getTemplateName())
                .items(items)
                .build();
    }
    
    /**
     * (B-1) 사전 체크리스트 진행 세션 시작
     * - 진행중 세션이 있으면 재사용
     * - 없으면 새로 생성
     */
    public PreChecklistDTO.SessionRes startOrGetSession(Long memberId) {

        // 1) ACTIVE PRE 템플릿 조회
        ChecklistTemplate template = templateRepo
                .findFirstByPhaseAndStatusOrderByVersionNoDesc(
                        Phase.PRE,
                        TemplateStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new IllegalStateException("ACTIVE 상태의 사전 체크리스트 템플릿이 없습니다.")
                );

        // 2) 진행중 세션 있으면 그대로 반환
        var existing = sessionRepo
                .findFirstByMemberIdAndPhaseAndStatus(
                        memberId,
                        Phase.PRE,
                        "IN_PROGRESS"
                );

        if (existing.isPresent()) {
            ChecklistSession session = existing.get();
            return PreChecklistDTO.SessionRes.builder()
                    .sessionId(session.getSessionId())
                    .templateId(session.getTemplate().getTemplateId())
                    .status(session.getStatus())
                    .build();
        }

        // 3) 없으면 새 세션 생성
        ChecklistSession newSession = ChecklistSession.builder()
                .memberId(memberId)
                .phase(Phase.PRE)
                .template(template)
                .status("IN_PROGRESS")
                .build();

        ChecklistSession saved = sessionRepo.save(newSession);

        return PreChecklistDTO.SessionRes.builder()
                .sessionId(saved.getSessionId())
                .templateId(template.getTemplateId())
                .status(saved.getStatus())
                .build();
    }

    /**
     * (C) 체크리스트 항목 체크 상태 저장 (upsert)
     * - DONE / NOT_DONE / NOT_REQUIRED
     */
    public void updateItemStatus(Long sessionId, Long itemId, PreChecklistDTO.UpdateItemReq req) {

        // 1) 세션 존재 확인
        ChecklistSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 sessionId=" + sessionId));

        // 2) 항목 존재 확인
        ChecklistItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 itemId=" + itemId));

        // 3) 상태값 검증(최소)
        String status = req.getCheckStatus();
        if (!"DONE".equals(status) && !"NOT_DONE".equals(status) && !"NOT_REQUIRED".equals(status)) {
            throw new IllegalArgumentException("checkStatus 값이 올바르지 않습니다: " + status);
        }

        // 4) (sessionId, itemId)로 기존 응답 조회 → 있으면 업데이트 / 없으면 생성
        ChecklistResponse response = responseRepo
                .findBySession_SessionIdAndItem_ItemId(sessionId, itemId)
                .orElseGet(() -> ChecklistResponse.builder()
                        .session(session)
                        .item(item)
                        .build()
                );

        response.setCheckStatus(status);
        responseRepo.save(response);
    }
    
    /**
     * (D) 세션 요약/경고
     * - 진행률
     * - 필수(required) 미완료 목록
     */
    public PreChecklistDTO.SummaryRes getSummary(Long sessionId) {

        // 1) 세션 조회 (templateId 얻기 위해)
        ChecklistSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 sessionId=" + sessionId));

        Long templateId = session.getTemplate().getTemplateId();

        // 2) 템플릿의 전체 아이템 조회(활성만)
        var items = itemRepo.findByTemplate_TemplateIdAndActiveYnOrderByItemOrderAsc(templateId, "Y");

        // 3) 해당 세션의 응답 조회
        var responses = responseRepo.findBySession_SessionId(sessionId);

        // 빠른 조회용 map
        java.util.Map<Long, String> statusMap = new java.util.HashMap<>();
        for (var r : responses) {
            statusMap.put(r.getItem().getItemId(), r.getCheckStatus());
        }

        int total = items.size();
        int done = 0;

        java.util.List<PreChecklistDTO.WarnItem> requiredNotDone = new java.util.ArrayList<>();

        for (var item : items) {
            String st = statusMap.get(item.getItemId()); // null이면 아직 체크 안함
            if ("DONE".equals(st)) done++;

            // requiredYn == "Y" 이고 DONE이 아니면 미완료로 판단
            if ("Y".equals(item.getRequiredYn())) {
                if (!"DONE".equals(st)) {
                    requiredNotDone.add(
                            PreChecklistDTO.WarnItem.builder()
                                    .itemId(item.getItemId())
                                    .title(item.getTitle())
                                    .build()
                    );
                }
            }
        }

        int requiredNotDoneCount = requiredNotDone.size();

        // 4) 레벨/메시지 규칙(간단 버전)
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

        return PreChecklistDTO.SummaryRes.builder()
                .totalCount(total)
                .doneCount(done)
                .requiredNotDoneCount(requiredNotDoneCount)
                .requiredNotDoneItems(requiredNotDone)
                .level(level)
                .message(message)
                .build();
    }


}
