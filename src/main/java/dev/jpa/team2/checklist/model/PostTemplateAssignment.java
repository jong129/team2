package dev.jpa.team2.checklist.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "POST_TEMPLATE_ASSIGNMENT",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UQ_POST_TEMPLATE_ASSIGNMENT",
            columnNames = {"PROFILE_KEY_ID", "POST_GROUP_CODE"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTemplateAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "SEQ_POST_TEMPLATE_ASSIGNMENT_ID")
    @SequenceGenerator(
        name = "SEQ_POST_TEMPLATE_ASSIGNMENT_ID",
        sequenceName = "SEQ_POST_TEMPLATE_ASSIGNMENT_ID",
        allocationSize = 1
    )
    @Column(name = "ASSIGNMENT_ID")
    private Long assignmentId;

    @Column(name = "PROFILE_KEY_ID", nullable = false)
    private Long profileKeyId;

    @Column(name = "POST_GROUP_CODE", nullable = false, length = 30)
    private String postGroupCode;

    @Column(name = "TEMPLATE_ID", nullable = false)
    private Long templateId;

    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private String activeYn = "Y";
}
