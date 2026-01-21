package dev.jpa.team2.checklist.dto;

import dev.jpa.team2.checklist.enums.CheckStatus;
import dev.jpa.team2.checklist.enums.Yn;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreChecklistItemDto {

    private Long itemId;
    private Integer itemOrder;
    private String title;
    private String description;
    private Yn requiredYn;  
    private CheckStatus checkStatus;

}
