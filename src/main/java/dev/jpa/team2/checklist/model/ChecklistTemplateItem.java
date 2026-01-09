package dev.jpa.team2.checklist.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHECKLIST_TEMPLATE_ITEM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChecklistTemplateItem {

  @EmbeddedId
  private ChecklistTemplateItemId id;

  @MapsId("templateId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TEMPLATE_ID", nullable = false)
  private ChecklistTemplate template;

  @MapsId("itemMasterId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ITEM_MASTER_ID", nullable = false)
  private ChecklistItemMaster itemMaster;

  @Column(name = "ITEM_ORDER", nullable = false)
  private Integer itemOrder;

  @Column(name = "REQUIRED_YN", nullable = false, length = 1)
  private String requiredYn; // Y/N

  @Column(name = "ACTIVE_YN", nullable = false, length = 1)
  private String activeYn; // Y/N

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "UPDATED_AT", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    if (requiredYn == null) requiredYn = "Y";
    if (activeYn == null) activeYn = "Y";
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
