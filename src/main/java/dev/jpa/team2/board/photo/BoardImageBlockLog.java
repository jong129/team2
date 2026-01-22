package dev.jpa.team2.board.photo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOARD_FILE_BLOCK_LOG")
@Getter
@Setter
public class BoardImageBlockLog {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BOARD_FILE_BLOCK_LOG_GEN")
  @SequenceGenerator(name = "SEQ_BOARD_FILE_BLOCK_LOG_GEN", sequenceName = "SEQ_BOARD_FILE_BLOCK_LOG_ID", allocationSize = 1)
  @Column(name = "LOG_ID")
  private Long logId;

  @Column(name = "MEMBER_ID", nullable = false)
  private Long memberId;

  @Column(name = "BOARD_ID", nullable = true)
  private Long boardId;

  @Column(name = "ORIGINAL_NAME", nullable = false, length = 500)
  private String originalName;

  @Column(name = "CONTENT_TYPE", length = 100)
  private String contentType;

  @Column(name = "FILE_SIZE", nullable = false)
  private Long fileSize = 0L;

  @Column(name = "ALLOWED_YN", nullable = false, length = 1)
  private String allowedYn = "N";

  @Column(name = "REASON_CODE", nullable = false, length = 50)
  private String reasonCode;

  @Column(name = "REASON_TEXT", length = 500)
  private String reasonText;

  @Column(name = "AI_TYPE", nullable = false, length = 30)
  private String aiType = "IMAGE_MODERATION";

  @Column(name = "PROMPT_CODE", length = 50)
  private String promptCode; // IMAGE_FILTER_V1 등

  @Lob
  @Column(name = "RAW_RESULT")
  private String rawResult; // JSON 원문(선택)

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null)
      createdAt = LocalDateTime.now();
  }
}
