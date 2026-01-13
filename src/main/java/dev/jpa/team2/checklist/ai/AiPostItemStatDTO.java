package dev.jpa.team2.checklist.ai;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AiPostItemStatDTO {
  private Long itemId;
  private Integer itemOrder;
  private String checkArea;
  private String title;
  private String requiredYn;

  private Long totalCnt;        // 표본(세션 수 기준)
  private Long doneCnt;
  private Long notDoneCnt;
  private Long notRequiredCnt;

  private Double doneRate;      // doneCnt / totalCnt
  private Double notDoneRate;
  private Double notRequiredRate;

  // 만족도 영향(있으면 좋음)
  private Double avgRatingWhenDone;
  private Double avgRatingWhenNotDone;
  private Double deltaRating;   // done평균 - notDone평균
}
