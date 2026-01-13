package dev.jpa.team2.checklist.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
  name = "CHECKLIST_SATISFACTION",
  uniqueConstraints = {
    @UniqueConstraint(name = "UQ_SAT_SESSION", columnNames = {"SESSION_ID"})
  }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistSatisfaction {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHECKLIST_SATISFACTION_ID")
  @SequenceGenerator(
    name = "SEQ_CHECKLIST_SATISFACTION_ID",
    sequenceName = "SEQ_CHECKLIST_SATISFACTION_ID",
    allocationSize = 1
  )
  @Column(name = "SATISFACTION_ID", nullable = false)
  private Long satisfactionId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "SESSION_ID", nullable = false)
  private ChecklistSession session;

  @Column(name = "RATING", nullable = false)
  private Integer rating;

  @Column(name = "COMMENT_TEXT", length = 2000)
  private String commentText;

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;
}
