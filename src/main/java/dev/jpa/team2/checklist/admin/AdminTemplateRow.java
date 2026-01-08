package dev.jpa.team2.checklist.admin;

import java.util.Date;
import java.time.LocalDateTime;

public interface AdminTemplateRow {
  Long getTemplateId();
  String getPhase();
  String getPostGroupCode();
  String getTemplateName();
  Integer getVersionNo();
  String getStatus();
  String getDescription();
  LocalDateTime getCreatedAt();
  LocalDateTime getUpdatedAt();
  Integer getActiveItemCnt();
  Integer getItemCnt();
}
