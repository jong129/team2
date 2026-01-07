package dev.jpa.team2.checklist.post;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateCheckStatusRequest {
    private String checkStatus; // DONE / NOT_DONE / NOT_REQUIRED
}
