package dev.jpa.team2.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardLikeToggleResponse {
  private boolean liked;   // 토글 후 상태
  private long likeCount;  // 현재 카운트

  public static BoardLikeToggleResponse of(boolean liked, long likeCount) {
    BoardLikeToggleResponse r = new BoardLikeToggleResponse();
    r.setLiked(liked);
    r.setLikeCount(likeCount);
    return r;
  }
}
