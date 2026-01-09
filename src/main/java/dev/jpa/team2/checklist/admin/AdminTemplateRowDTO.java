package dev.jpa.team2.checklist.admin;

import java.time.LocalDateTime;

public class AdminTemplateRowDTO implements AdminTemplateRow {

  private final Long templateId;
  private final String phase;
  private final String postGroupCode;
  private final String templateName;
  private final Integer versionNo;
  private final String status;
  private final String description;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;
  private final Integer activeItemCnt;
  private final Integer itemCnt;

  public AdminTemplateRowDTO(
      Long templateId,
      String phase,
      String postGroupCode,
      String templateName,
      Integer versionNo,
      String status,
      String description,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      Integer activeItemCnt,
      Integer itemCnt
  ) {
    this.templateId = templateId;
    this.phase = phase;
    this.postGroupCode = postGroupCode;
    this.templateName = templateName;
    this.versionNo = versionNo;
    this.status = status;
    this.description = description;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.activeItemCnt = activeItemCnt;
    this.itemCnt = itemCnt;
  }

  @Override public Long getTemplateId() { return templateId; }
  @Override public String getPhase() { return phase; }
  @Override public String getPostGroupCode() { return postGroupCode; }
  @Override public String getTemplateName() { return templateName; }
  @Override public Integer getVersionNo() { return versionNo; }
  @Override public String getStatus() { return status; }
  @Override public String getDescription() { return description; }
  @Override public LocalDateTime getCreatedAt() { return createdAt; }
  @Override public LocalDateTime getUpdatedAt() { return updatedAt; }
  @Override public Integer getActiveItemCnt() { return activeItemCnt; }
  @Override public Integer getItemCnt() { return itemCnt; }
}
