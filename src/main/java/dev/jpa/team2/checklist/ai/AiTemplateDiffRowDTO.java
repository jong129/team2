package dev.jpa.team2.checklist.ai;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiTemplateDiffRowDTO {

  private Integer itemOrder;

  // 기존 템플릿
  private Long beforeMasterId;    // 프론트에서 안 쓰면 나중에 제거 가능
  private String beforeTitle;

  // AI 초안 템플릿
  private Long afterMasterId;
  private String afterTitle;

  // AI 판단 결과
  private PostChecklistSignalType action; // KEEP / IMPROVE_COPY / INSIGHT_CANDIDATE / REMOVE_CANDIDATE
  private String reason;                  // 왜 이렇게 판단했는지
}
