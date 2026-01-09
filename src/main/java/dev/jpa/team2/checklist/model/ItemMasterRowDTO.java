package dev.jpa.team2.checklist.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemMasterRowDTO {
  private Long itemMasterId;
  private Phase phase;
  private String postGroupCode;
  private String title;
  private String description;
  private String activeYn;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
