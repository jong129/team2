package dev.jpa.team2.checklist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TemplateItemRowDTO {
  private Long itemMasterId;
  private Integer itemOrder;
  private String requiredYn;
  private String activeYn;

  // master info
  private Phase phase;
  private String postGroupCode;
  private String title;
  private String description;
}
