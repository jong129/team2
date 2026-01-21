package dev.jpa.team2.board.category;

import lombok.Data;

@Data
public class BoardCategoryUpdateRequest {
  private String categoryName;

  private String visibleYn;
  private BoardCategoryWritePolicy writePolicy;

  private String commentYn;
  private String reportYn;
  private String likeYn;
  private String secretYn;
  private String fileYn;

  private String aiSummaryYn;
  private String aiSentimentYn;
  private String aiWriteYn;

  private Integer sortNo;
}
