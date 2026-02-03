package dev.jpa.team2.checklist.model;

import java.util.Date;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "POST_CHECKLIST_SUMMARY")
@Getter
@Setter
@SequenceGenerator(
    name = "POST_CHECKLIST_SUMMARY_SEQ_GEN",
    sequenceName = "SEQ_POST_CHECKLIST_SUMMARY_ID",
    allocationSize = 1
)
public class PostChecklistSummary {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "POST_CHECKLIST_SUMMARY_SEQ_GEN"
    )
    @Column(name = "SUMMARY_ID")
    private Long summaryId;

    @Column(name = "SESSION_ID", nullable = false)
    private Long sessionId;

    @Column(name = "SUMMARY_TEXT", nullable = false, length = 1000)
    private String summaryText;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;
    
    @Column(name = "GUIDES_JSON", columnDefinition = "CLOB")
    private String guidesJson;
}

