package dev.jpa.team2.board.file;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOARD_FILE")
@Getter
@Setter
public class BoardFile {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BOARD_FILE_ID_GEN")
  @SequenceGenerator(
      name = "SEQ_BOARD_FILE_ID_GEN",
      sequenceName = "SEQ_BOARD_FILE_ID",
      allocationSize = 1
  )
  @Column(name = "FILE_ID")
  private Long fileId;

  @Column(name = "BOARD_ID", nullable = false)
  private Long boardId;

  @Column(name = "ORIGINAL_NAME", nullable = false, length = 255)
  private String originalName;

  @Column(name = "SAVED_NAME", nullable = false, length = 255)
  private String savedName;

  @Column(name = "FILE_SIZE", nullable = false)
  private Long fileSize;

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = LocalDateTime.now();
    if (fileSize == null) fileSize = 0L;
  }
}
