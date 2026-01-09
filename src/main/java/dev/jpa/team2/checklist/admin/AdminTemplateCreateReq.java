package dev.jpa.team2.checklist.admin;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AdminTemplateCreateReq {
  private String phase;         // "PRE" | "POST"
  private String postGroupCode; // POST일 때만 (예: "POST_A")
  private String templateName;
  private String description;
}
