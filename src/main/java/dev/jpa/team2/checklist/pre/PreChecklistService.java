package dev.jpa.team2.checklist.pre;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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
}
