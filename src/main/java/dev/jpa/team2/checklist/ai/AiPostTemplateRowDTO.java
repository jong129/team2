package dev.jpa.team2.checklist.ai;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AiPostTemplateRowDTO {
  private Long templateId;
  private String postGroupCode;
  private Integer versionNo;
  private String templateName;

  private Long completedSessionCnt;   // 완료 세션 수
  private Double avgRating;           // 평균 만족도(별점)
}
