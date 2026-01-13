package dev.jpa.team2.board;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BoardPostCreateRequest {
  private Long categoryId;
  private String title;
  private String content;

  // secret 글(카테고리 secretYn=Y일 때만 허용)
  private String secretYn;        // Y/N ✅ 통일
  private String postPassword;    // 선택(저장)
}
