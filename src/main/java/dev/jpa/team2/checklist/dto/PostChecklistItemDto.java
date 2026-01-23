package dev.jpa.team2.checklist.dto;

import dev.jpa.team2.checklist.enums.Yn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostChecklistItemDto {

    private Long itemId;
    private String checkArea;
    private String title;
    private String description;
    private Yn requiredYn;

}
