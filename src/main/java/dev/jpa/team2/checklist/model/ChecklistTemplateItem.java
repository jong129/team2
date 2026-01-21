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
@Table(
    name = "CHECKLIST_TEMPLATE_ITEM",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"TEMPLATE_ID", "ITEM_ORDER"})
    }
)
public class ChecklistTemplateItem {

    /** ✅ 복합 PK */
    @EmbeddedId
    private ChecklistTemplateItemId id;

    /** ✅ 템플릿 (PK 일부) */
    @MapsId("templateId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    private ChecklistTemplate template;

    /** ✅ 아이템 마스터 (PK 일부) */
    @MapsId("itemMasterId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_MASTER_ID", nullable = false)
    private ChecklistItemMaster itemMaster;

    @Column(name = "ITEM_ORDER", nullable = false)
    private Integer itemOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "REQUIRED_YN", length = 1)
    private Yn requiredYn = Yn.Y;

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
