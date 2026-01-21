package dev.jpa.team2.checklist.dto;

import dev.jpa.team2.checklist.enums.Yn;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreChecklistSessionItemDto {

    private Long itemId;        // CHECKLIST_ITEM_ID (⭐️)
    private String checkArea;   // 없으면 null
    private String title;
    private String description;
    private Yn requiredYn;
}
