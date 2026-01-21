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
@Table(name = "POST_TEMPLATE_ASSIGNMENT",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"PROFILE_KEY_ID", "POST_GROUP_CODE"})
       })
@SequenceGenerator(
    name = "SEQ_POST_TEMPLATE_ASSIGNMENT_GEN",
    sequenceName = "SEQ_POST_TEMPLATE_ASSIGNMENT_ID",
    allocationSize = 1
)
public class PostTemplateAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_POST_TEMPLATE_ASSIGNMENT_GEN")
    @Column(name = "ASSIGNMENT_ID")
    private Long assignmentId;

    @Column(name = "PROFILE_KEY_ID", nullable = false)
    private Long profileKeyId;

    @Column(name = "POST_GROUP_CODE", length = 30, nullable = false)
    private String postGroupCode;

    @Column(name = "TEMPLATE_ID", nullable = false)
    private Long templateId;

    @Column(name = "ACTIVE_YN", length = 1)
    private String activeYn = "Y";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_AT", nullable = false)
    private Date updatedAt = new Date();
}
