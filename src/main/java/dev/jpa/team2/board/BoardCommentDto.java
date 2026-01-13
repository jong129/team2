package dev.jpa.team2.board;

import dev.jpa.team2.board.comment.BoardComment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardCommentDto {
  private Long commentId;
  private Long boardId;
  private Long memberId;
  private Long parentId;
  private String content;
  private String isSecret;

  public static BoardCommentDto from(BoardComment c) {
    BoardCommentDto d = new BoardCommentDto();
    d.setCommentId(c.getCommentId());
    d.setBoardId(c.getBoardId());
    d.setMemberId(c.getMemberId());
    d.setParentId(c.getParentId());
    d.setContent(c.getContent());
    d.setIsSecret(c.getIsSecret());
    return d;
  }
}
