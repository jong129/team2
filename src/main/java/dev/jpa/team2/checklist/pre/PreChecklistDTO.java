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
    
    /**
     * 사전 체크리스트 세션 시작/조회 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionRes {
        private Long sessionId;
        private Long templateId;
        private String status; // IN_PROGRESS / COMPLETED
    }

    /**
     * 체크 상태 업데이트 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateItemReq {
        private String checkStatus; // DONE / NOT_DONE / NOT_REQUIRED
    }

    /**
     * (D) 요약/경고 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryRes {
        private int totalCount;
        private int doneCount;

        private int requiredNotDoneCount;
        private List<WarnItem> requiredNotDoneItems;

        private String level;   // INFO / WARN / DANGER
        private String message; // 안내/권고/경고 문구
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarnItem {
        private Long itemId;
        private String title;
    }

    
}
