package dev.jpa.team2.checklist.model;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ChecklistTemplateItemId implements Serializable {

    @Column(name = "TEMPLATE_ID")
    private Long templateId;

    @Column(name = "ITEM_MASTER_ID")
    private Long itemMasterId;
}
