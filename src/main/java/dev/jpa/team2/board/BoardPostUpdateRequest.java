package dev.jpa.team2.board;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BoardPostUpdateRequest {
  private String title;
  private String content;

  private String secretYn;        // Y/N ✅ 통일
  private String postPassword;    // 선택
  private String pinnedYn;        // Y/N (admin만 허용: 서비스에서 제어)
}
