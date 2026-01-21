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
@Table(name = "CHECKLIST_ITEM_MASTER")
@SequenceGenerator(
    name = "SEQ_CHECKLIST_ITEM_MASTER_GEN",
    sequenceName = "SEQ_CHECKLIST_ITEM_MASTER_ID",
    allocationSize = 1
)
public class ChecklistItemMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHECKLIST_ITEM_MASTER_GEN")
    @Column(name = "ITEM_MASTER_ID")
    private Long itemMasterId;

    @Column(name = "PHASE", length = 10)
    private String phase;

    @Column(name = "POST_GROUP_CODE", length = 30)
    private String postGroupCode;

    @Column(name = "CHECK_AREA", length = 50)
    private String checkArea;

    @Column(name = "TITLE", length = 200, nullable = false)
    private String title;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "ACTIVE_YN", length = 1)
    private String activeYn = "Y";

    @Lob
    @Column(name = "TAGS_JSON")
    private String tagsJson;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_AT", nullable = false)
    private Date updatedAt = new Date();
}
