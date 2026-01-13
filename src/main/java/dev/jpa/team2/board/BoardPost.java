package dev.jpa.team2.board;

import dev.jpa.team2.board.category.BoardCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOARD")
@Getter
@Setter
public class BoardPost {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BOARD_ID_GEN")
  @SequenceGenerator(name = "SEQ_BOARD_ID_GEN", sequenceName = "SEQ_BOARD_ID", allocationSize = 1)
  @Column(name = "BOARD_ID")
  private Long boardId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "CATEGORY_ID", nullable = false)
  private BoardCategory category;

  @Column(name = "MEMBER_ID", nullable = false)
  private Long memberId;

  @Column(name = "LOGIN_ID", nullable = false, length = 50)
  private String loginId;

  @Column(name = "WRITER_NAME", nullable = false, length = 100)
  private String writerName;

  @Column(name = "TITLE", nullable = false, length = 200)
  private String title;

  @Lob
  @Column(name = "CONTENT", nullable = false)
  private String content;

  // ✅ 비밀글 여부 (DB: SECRET_YN)
  @Column(name = "SECRET_YN", nullable = false, length = 1)
  private String secretYn = "N";

  // 비밀글 비밀번호(선택)
  @Column(name = "POST_PASSWORD", length = 100)
  private String postPassword;

  // 상단고정(공지 등)
  @Column(name = "PINNED_YN", nullable = false, length = 1)
  private String pinnedYn = "N";

  // 소프트삭제
  @Column(name = "DELETED_YN", nullable = false, length = 1)
  private String deletedYn = "N";

  @Column(name = "VIEW_CNT", nullable = false)
  private Long viewCnt = 0L;

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "UPDATED_AT")
  private LocalDateTime updatedAt;

  @PrePersist
  void onCreate() {
    this.createdAt = LocalDateTime.now();
    if (this.viewCnt == null) this.viewCnt = 0L;
    if (this.secretYn == null) this.secretYn = "N";
    if (this.pinnedYn == null) this.pinnedYn = "N";
    if (this.deletedYn == null) this.deletedYn = "N";

    // secretYn=N이면 비번 제거(정합성)
    if (!"Y".equalsIgnoreCase(this.secretYn)) {
      this.postPassword = null;
    }
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = LocalDateTime.now();

    // secretYn=N이면 비번 제거(정합성)
    if (!"Y".equalsIgnoreCase(this.secretYn)) {
      this.postPassword = null;
    }
  }
}

