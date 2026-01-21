package dev.jpa.team2.checklist.model;

import java.util.Date;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "CHECKLIST_SATISFACTION",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_CHECKLIST_SATISFACTION", columnNames = "SESSION_ID")
    }
)
@SequenceGenerator(
    name = "SEQ_CHECKLIST_SATISFACTION_GEN",
    sequenceName = "SEQ_CHECKLIST_SATISFACTION_ID",
    allocationSize = 1
)
public class ChecklistSatisfaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHECKLIST_SATISFACTION_GEN")
    @Column(name = "SATISFACTION_ID")
    private Long satisfactionId;

    @Column(name = "SESSION_ID", nullable = false)
    private Long sessionId;

    @Column(name = "RATING", nullable = false)
    private Integer rating;

    @Column(name = "COMMENT_TEXT", length = 1000)
    private String commentText;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;
}
