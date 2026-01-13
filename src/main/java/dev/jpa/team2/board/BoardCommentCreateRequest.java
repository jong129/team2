package dev.jpa.team2.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardCommentCreateRequest {
  private Long parentId;     // null이면 댓글, 값 있으면 대댓글
  private String content;
  private String isSecret;   // optional (기본 N)
}
