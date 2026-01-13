package dev.jpa.team2.board.like;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOARD_LIKE")
@Getter
@Setter
public class BoardLike {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BOARD_LIKE_ID_GEN")
  @SequenceGenerator(
      name = "SEQ_BOARD_LIKE_ID_GEN",
      sequenceName = "SEQ_BOARD_LIKE_ID",
      allocationSize = 1
  )
  @Column(name = "LIKE_ID")
  private Long likeId;

  @Column(name = "BOARD_ID", nullable = false)
  private Long boardId;

  @Column(name = "MEMBER_ID", nullable = false)
  private Long memberId;

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = LocalDateTime.now();
  }
}
