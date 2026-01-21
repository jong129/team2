package dev.jpa.team2.checklist.model;

import java.util.Date;

import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.SessionStatus;
import dev.jpa.team2.checklist.enums.Yn;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CHECKLIST_SESSION")
@SequenceGenerator(
    name = "SEQ_CHECKLIST_SESSION_GEN",
    sequenceName = "SEQ_CHECKLIST_SESSION_ID",
    allocationSize = 1
)
public class ChecklistSession {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHECKLIST_SESSION_GEN")
    @Column(name = "SESSION_ID")
    private Long sessionId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "PHASE", length = 10, nullable = false)
    private ChecklistPhase phase;

    @Column(name = "TEMPLATE_ID", nullable = false)
    private Long templateId;
    
    @Column(name = "PRE_SESSION_ID")
    private Long preSessionId;

    @Column(name = "PROFILE_KEY_ID")
    private Long profileKeyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "STARTED_AT", nullable = false)
    private Date startedAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "COMPLETED_AT")
    private Date completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "DELETED_YN", length = 1, nullable = false)
    private Yn deletedYn = Yn.N;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DELETED_AT")
    private Date deletedAt;
}
