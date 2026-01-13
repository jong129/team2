package dev.jpa.team2.checklist.ai;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTemplateImproveResponse {
  private Long baseTemplateId;
  private Long newTemplateId;
  private Integer newVersionNo;
  private String message;
}
