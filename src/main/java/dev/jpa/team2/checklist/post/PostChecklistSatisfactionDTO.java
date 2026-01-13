package dev.jpa.team2.checklist.post;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostChecklistSatisfactionDTO {
  private Long sessionId;
  private Integer rating;
  private String commentText;
  private String createdAt;
}
