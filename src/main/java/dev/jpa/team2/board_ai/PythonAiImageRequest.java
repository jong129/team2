package dev.jpa.team2.board_ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PythonAiImageRequest {
  private String prompt;
  private String imageBase64;
  private String filename;
  private String contentType;

  private boolean jsonMode = true;
  private int maxTokens = 8000;
}
