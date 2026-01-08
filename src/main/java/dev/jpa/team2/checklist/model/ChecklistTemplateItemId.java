package dev.jpa.team2.checklist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChecklistTemplateItemId implements Serializable {

  @Column(name = "TEMPLATE_ID")
  private Long templateId;

  @Column(name = "ITEM_MASTER_ID")
  private Long itemMasterId;
}
