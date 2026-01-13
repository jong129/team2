package dev.jpa.team2.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardLikeCountResponse {
  private long likeCount;

  public static BoardLikeCountResponse of(long likeCount) {
    BoardLikeCountResponse r = new BoardLikeCountResponse();
    r.setLikeCount(likeCount);
    return r;
  }
}
