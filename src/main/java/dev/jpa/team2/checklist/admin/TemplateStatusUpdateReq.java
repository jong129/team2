package dev.jpa.team2.checklist.admin;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TemplateStatusUpdateReq {
  private String status; // DRAFT | ACTIVE | RETIRED
}
