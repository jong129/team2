package dev.jpa.team2.checklist.admin.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPreviewResponse {

    private List<AiPreviewItem> newItems;

    @Getter
    @Setter
    public static class AiPreviewItem {
        private String title;
        private String description;
        private String source;
    }
}
