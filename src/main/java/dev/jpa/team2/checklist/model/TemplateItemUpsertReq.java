package dev.jpa.team2.checklist.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TemplateItemUpsertReq {
  private Long itemMasterId;
  private Integer itemOrder;
  private String requiredYn; // Y/N
  private String activeYn;   // Y/N
}
