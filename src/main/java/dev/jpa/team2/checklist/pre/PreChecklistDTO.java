package dev.jpa.team2.checklist.pre;

import lombok.*;

import java.util.List;

public class PreChecklistDTO {

    /**
     * 체크리스트 항목 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemRes {
        private Long itemId;
        private Integer itemOrder;
        private String checkArea;
        private String title;
        private String description;
    }

    /**
     * 사전 체크리스트 전체 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreChecklistRes {
        private Long templateId;
        private String templateName;
        private List<ItemRes> items;
    }
}
