package dev.jpa.team2.checklist.admin;

import java.time.LocalDateTime;

import dev.jpa.team2.checklist.model.Phase;
import dev.jpa.team2.checklist.model.TemplateStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AdminTemplateDetailDTO {
  private Long templateId;
  private Phase phase;
  private String postGroupCode;
  private String templateName;
  private Integer versionNo;
  private TemplateStatus status;
  private String description;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
