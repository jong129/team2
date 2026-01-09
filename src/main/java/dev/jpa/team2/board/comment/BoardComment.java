package dev.jpa.team2.board.comment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOARD_COMMENT")
@Getter
@Setter
public class BoardComment {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BOARD_COMMENT_ID_GEN")
  @SequenceGenerator(
      name = "SEQ_BOARD_COMMENT_ID_GEN",
      sequenceName = "SEQ_BOARD_COMMENT_ID",
      allocationSize = 1
  )
  @Column(name = "COMMENT_ID")
  private Long commentId;

  @Column(name = "BOARD_ID", nullable = false)
  private Long boardId;

  @Column(name = "MEMBER_ID", nullable = false)
  private Long memberId;

  @Column(name = "PARENT_ID")
  private Long parentId;

  @Column(name = "CONTENT", nullable = false, length = 1000)
  private String content;

  @Column(name = "IS_SECRET", nullable = false, length = 1)
  private String isSecret; // Y/N

  @Column(name = "DELETED_YN", nullable = false, length = 1)
  private String deletedYn; // Y/N (우리는 하드삭제라 실사용 X)

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "UPDATED_AT")
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    if (isSecret == null) isSecret = "N";
    if (deletedYn == null) deletedYn = "N";
    if (createdAt == null) createdAt = LocalDateTime.now();
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
