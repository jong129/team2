package dev.jpa.team2.checklist.ai;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AiPostItemSignalDTO {

  private Long itemId;
  private Integer itemOrder;
  private String title;
  private String requiredYn;

  private PostChecklistSignalType signal;
  private String reason;   // 관리자에게 보여줄 설명
}
