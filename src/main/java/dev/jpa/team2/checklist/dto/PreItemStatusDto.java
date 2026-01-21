package dev.jpa.team2.checklist.dto;

import dev.jpa.team2.checklist.enums.CheckStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreItemStatusDto {

    private Long itemId;
    private CheckStatus checkStatus;

}
