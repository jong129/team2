package dev.jpa.team2.checklist.post;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostChecklistSummaryDTO {
  private String level;   // OK / WARN / RISK 같은 텍스트
  private String message; // 화면에 보여줄 문구
}
