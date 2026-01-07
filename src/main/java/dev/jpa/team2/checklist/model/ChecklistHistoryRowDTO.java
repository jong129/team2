package dev.jpa.team2.checklist.model;

import java.time.LocalDateTime;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChecklistHistoryRowDTO {
  private Long sessionId;
  private String phase;      // "PRE"/"POST"
  private String status;     // "IN_PROGRESS"/"COMPLETED"
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
}
