package dev.jpa.team2.checklist.model;

import java.util.Date;

import dev.jpa.team2.checklist.enums.CheckStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CHECKLIST_RESPONSE",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"SESSION_ID", "ITEM_ID"})
       })
@SequenceGenerator(
    name = "SEQ_CHECKLIST_RESPONSE_GEN",
    sequenceName = "SEQ_CHECKLIST_RESPONSE_ID",
    allocationSize = 1
)
public class ChecklistResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHECKLIST_RESPONSE_GEN")
    @Column(name = "RESPONSE_ID")
    private Long responseId;

    @Column(name = "SESSION_ID", nullable = false)
    private Long sessionId;

    @Column(name = "ITEM_ID", nullable = false)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "CHECK_STATUS", length = 20)
    private CheckStatus checkStatus;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_AT", nullable = false)
    private Date updatedAt = new Date();
}
