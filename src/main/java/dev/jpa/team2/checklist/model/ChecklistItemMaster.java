package dev.jpa.team2.checklist.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHECKLIST_ITEM_MASTER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChecklistItemMaster {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHECKLIST_ITEM_MASTER_ID")
  @SequenceGenerator(
      name = "SEQ_CHECKLIST_ITEM_MASTER_ID",
      sequenceName = "SEQ_CHECKLIST_ITEM_MASTER_ID",
      allocationSize = 1
  )
  @Column(name = "ITEM_MASTER_ID")
  private Long itemMasterId;

  @Enumerated(EnumType.STRING)
  @Column(name = "PHASE", nullable = false, length = 10)
  private Phase phase; // PRE/POST

  @Column(name = "POST_GROUP_CODE", length = 30)
  private String postGroupCode; // POST_A/B/C/D or null

  @Column(name = "TITLE", nullable = false, length = 200)
  private String title;

  @Column(name = "DESCRIPTION", length = 1000)
  private String description;

  @Lob
  @Column(name = "TAGS_JSON")
  private String tagsJson;

  @Column(name = "ACTIVE_YN", nullable = false, length = 1)
  private String activeYn; // Y/N

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "UPDATED_AT", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    if (activeYn == null) activeYn = "Y";
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public void changeActive(String yn) {
    this.activeYn = yn;
    this.updatedAt = LocalDateTime.now();
  }

  public void updateText(String title, String description, String tagsJson) {
    this.title = title;
    this.description = description;
    this.tagsJson = tagsJson;
    this.updatedAt = LocalDateTime.now();
  }
}
