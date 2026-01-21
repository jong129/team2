package dev.jpa.team2.checklist.dto;

import java.util.List;

import dev.jpa.team2.checklist.enums.CheckStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreChecklistSyncRequest {

    private List<ItemSyncDto> items;

    @Getter
    @Setter
    public static class ItemSyncDto {
        private Long itemId;
        private CheckStatus checkStatus;
    }
}
