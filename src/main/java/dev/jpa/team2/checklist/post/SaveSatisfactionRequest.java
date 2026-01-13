package dev.jpa.team2.checklist.post;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveSatisfactionRequest {

  private Integer rating;      // 1~5
  private String commentText;  // optional
}
