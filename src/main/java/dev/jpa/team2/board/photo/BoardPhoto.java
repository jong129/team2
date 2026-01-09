package dev.jpa.team2.board.photo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOARD_PHOTO")
@Getter @Setter
public class BoardPhoto {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BOARD_PHOTO_ID_GEN")
  @SequenceGenerator(
      name = "SEQ_BOARD_PHOTO_ID_GEN",
      sequenceName = "SEQ_BOARD_PHOTO_ID",
      allocationSize = 1
  )
  @Column(name = "PHOTO_ID")
  private Long photoId;

  @Column(name = "BOARD_ID", nullable = false)
  private Long boardId;

  @Column(name = "SAVED_NAME", nullable = false, length = 255)
  private String savedName;

  @Column(name = "THUMB_NAME", length = 255)
  private String thumbName;

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = LocalDateTime.now();
  }
}
