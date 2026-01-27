package dev.jpa.team2.checklist.ai.dto;

import java.util.List;
import java.util.Map;

import dev.jpa.team2.checklist.model.ChecklistItem;
import dev.jpa.team2.checklist.model.ChecklistResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 중요도 스코어링 요청 DTO
 */
@Getter
@Setter
public class ChecklistScoreRequest {

    /** 점수 계산 대상 항목 목록 */
    private List<ChecklistScoreItem> items;

    /**
     * ChecklistResponse + ChecklistItem 정보를
     * AI 중요도 스코어링 요청 DTO로 변환
     *
     * @param responses NOT_DONE 응답 목록
     * @param itemMap   itemId -> ChecklistItem 매핑
     */
    public static ChecklistScoreRequest from(
            List<ChecklistResponse> responses,
            Map<Long, ChecklistItem> itemMap
    ) {

        ChecklistScoreRequest request = new ChecklistScoreRequest();

        List<ChecklistScoreItem> items = responses.stream()
            .map(response -> {
                ChecklistItem item = itemMap.get(response.getItemId());

                ChecklistScoreItem dto = new ChecklistScoreItem();
                dto.setItemId(response.getItemId());
                dto.setTitle(item != null ? item.getTitle() : "알 수 없는 항목");
                dto.setDescription(item != null ? item.getDescription() : "");

                return dto;
            })
            .toList();

        request.setItems(items);
        return request;
    }
}
