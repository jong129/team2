package dev.jpa.team2.board.report;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOARD_REPORT")
@Getter
@Setter
public class BoardReport {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BOARD_REPORT_ID_GEN")
  @SequenceGenerator(
      name = "SEQ_BOARD_REPORT_ID_GEN",
      sequenceName = "SEQ_BOARD_REPORT_ID",
      allocationSize = 1
  )
  @Column(name = "REPORT_ID")
  private Long reportId;

  @Column(name = "BOARD_ID", nullable = false)
  private Long boardId;

  @Column(name = "MEMBER_ID", nullable = false)
  private Long memberId;

  @Column(name = "REASON_CODE", nullable = false, length = 50)
  private String reasonCode;

  @Column(name = "REASON_TEXT", length = 500)
  private String reasonText;

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = LocalDateTime.now();
  }
}
