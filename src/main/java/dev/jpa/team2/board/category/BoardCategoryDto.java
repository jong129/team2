package dev.jpa.team2.board.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class BoardCategoryDto {

  private Long categoryId;
  private String categoryName;

  private String visibleYn;
  private BoardCategoryWritePolicy writePolicy;

  private String commentYn;
  private String reportYn;
  private String likeYn;
  private String secretYn;
  private String fileYn;

  private Integer sortNo;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private String aiSummaryYn;
  private String aiSentimentYn;
  private String aiWriteYn;

  public static BoardCategoryDto fromEntity(BoardCategory e) {
    return BoardCategoryDto.builder()
        .categoryId(e.getCategoryId())
        .categoryName(e.getCategoryName())
        .visibleYn(e.getVisibleYn())
        .writePolicy(e.getWritePolicy())
        .commentYn(e.getCommentYn())
        .reportYn(e.getReportYn())
        .likeYn(e.getLikeYn())
        .secretYn(e.getSecretYn())
        .fileYn(e.getFileYn())
        .sortNo(e.getSortNo())
        .createdAt(e.getCreatedAt())
        .updatedAt(e.getUpdatedAt())
        .aiSummaryYn(e.getAiSummaryYn())
        .aiSentimentYn(e.getAiSentimentYn())
        .aiWriteYn(e.getAiWriteYn())
        .build();
  }
}
