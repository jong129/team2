package dev.jpa.team2.checklist.dto;

import dev.jpa.team2.checklist.enums.Yn;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreChecklistTemplateItemDto {

    private Long itemId;
    private String checkArea;
    private String title;
    private String description;
    private Yn requiredYn;

}
