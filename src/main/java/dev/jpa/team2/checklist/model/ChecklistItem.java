package dev.jpa.team2.checklist.model;

import java.util.Date;

import dev.jpa.team2.checklist.enums.Yn;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CHECKLIST_ITEM",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"SESSION_ID", "ITEM_ORDER"})
       })
@SequenceGenerator(
    name = "SEQ_CHECKLIST_ITEM_GEN",
    sequenceName = "SEQ_CHECKLIST_ITEM_ID",
    allocationSize = 1
)
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHECKLIST_ITEM_GEN")
    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "SESSION_ID", nullable = false)
    private Long sessionId;

    @Column(name = "ITEM_ORDER", nullable = false)
    private Integer itemOrder;

    @Column(name = "CHECK_AREA", length = 50)
    private String checkArea;

    @Column(name = "TITLE", length = 300, nullable = false)
    private String title;

    @Column(name = "DESCRIPTION", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "REQUIRED_YN", length = 1)
    private Yn requiredYn = Yn.N;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACTIVE_YN", length = 1)
    private Yn activeYn = Yn.Y;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_AT", nullable = false)
    private Date updatedAt = new Date();
}
