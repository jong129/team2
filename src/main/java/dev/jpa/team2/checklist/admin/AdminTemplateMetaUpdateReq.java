package dev.jpa.team2.checklist.admin;

import dev.jpa.team2.checklist.model.TemplateStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AdminTemplateMetaUpdateReq {
  private String templateName;
  private String description;
  private TemplateStatus status; // DRAFT / ACTIVE / RETIRED
}
