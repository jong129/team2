package dev.jpa.team2.board.category;

import lombok.Data;

@Data
public class BoardCategoryCreateRequest {
  private String categoryName;

  private String visibleYn;
  private BoardCategoryWritePolicy writePolicy;

  private String commentYn;
  private String reportYn;
  private String likeYn;
  private String secretYn;
  private String fileYn;

  private Integer sortNo;
}
