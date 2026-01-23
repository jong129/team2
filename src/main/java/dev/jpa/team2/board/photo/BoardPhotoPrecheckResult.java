package dev.jpa.team2.board.photo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardPhotoPrecheckResult {
  private String filename;
  private String sha256;      // ✅ 추가: 파일 해시
  private boolean allowed;
  private String reasonCode;  // AD|COMMERCIAL|SEXUAL|VIOLENCE|HATE|OTHER 등
  private String reasonText;  // 한글 1줄
  private Double score;       // 0.0~1.0 (없으면 null)
}
