package dev.jpa.team2.board.category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOARD_CATEGORY")
@Getter
@Setter
public class BoardCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BOARD_CATEGORY_ID_GEN")
  @SequenceGenerator(
      name = "SEQ_BOARD_CATEGORY_ID_GEN",
      sequenceName = "SEQ_BOARD_CATEGORY_ID",
      allocationSize = 1
  )
  @Column(name = "CATEGORY_ID")
  private Long categoryId;

  @Column(name = "CATEGORY_NAME", nullable = false, length = 100)
  private String categoryName;

  @Column(name = "VISIBLE_YN", nullable = false, length = 1)
  private String visibleYn; // Y/N

  @Enumerated(EnumType.STRING)
  @Column(name = "WRITE_POLICY", nullable = false, length = 20)
  private BoardCategoryWritePolicy writePolicy; // ADMIN_ONLY / LOGIN_ANY

  @Column(name = "COMMENT_YN", nullable = false, length = 1)
  private String commentYn;

  @Column(name = "REPORT_YN", nullable = false, length = 1)
  private String reportYn;

  @Column(name = "LIKE_YN", nullable = false, length = 1)
  private String likeYn;

  @Column(name = "SECRET_YN", nullable = false, length = 1)
  private String secretYn;

  @Column(name = "FILE_YN", nullable = false, length = 1)
  private String fileYn;

  @Column(name = "SORT_NO", nullable = false)
  private Integer sortNo;

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "UPDATED_AT")
  private LocalDateTime updatedAt;

  @Column(name = "AI_SUMMARY_YN", nullable = false, length = 1)
  private String aiSummaryYn;

  @Column(name = "AI_SENTIMENT_YN", nullable = false, length = 1)
  private String aiSentimentYn;

  @Column(name = "AI_WRITE_YN", nullable = false, length = 1)
  private String aiWriteYn;

  @PrePersist
  void prePersist() {
    if (visibleYn == null) visibleYn = "Y";
    if (writePolicy == null) writePolicy = BoardCategoryWritePolicy.LOGIN_ANY;

    if (commentYn == null) commentYn = "Y";
    if (reportYn == null) reportYn = "Y";
    if (likeYn == null) likeYn = "Y";
    if (secretYn == null) secretYn = "N";
    if (fileYn == null) fileYn = "Y";

    if (sortNo == null) sortNo = 0;
    if (createdAt == null) createdAt = LocalDateTime.now();

    if (aiSummaryYn == null) aiSummaryYn = "N";
    if (aiSentimentYn == null) aiSentimentYn = "N";
    if (aiWriteYn == null) aiWriteYn = "N";
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
