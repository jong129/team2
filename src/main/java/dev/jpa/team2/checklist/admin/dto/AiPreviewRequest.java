package dev.jpa.team2.checklist.admin.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPreviewRequest {

    private List<String> baseItems;
    private String phase;
}
