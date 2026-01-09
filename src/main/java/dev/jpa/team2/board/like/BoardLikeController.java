package dev.jpa.team2.board.like;

import dev.jpa.team2.board.BoardLikeCountResponse;
import dev.jpa.team2.board.BoardLikeToggleResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardLikeController {

  private final BoardLikeService boardLikeService;

  @PostMapping("/posts/{boardId}/likes/toggle")
  public ResponseEntity<BoardLikeToggleResponse> toggle(
      @PathVariable("boardId") Long boardId,
      HttpSession session
  ) {
    Long memberId = getLoginMemberId(session);
    return ResponseEntity.ok(boardLikeService.toggle(boardId, memberId));
  }

  @GetMapping("/posts/{boardId}/likes/count")
  public ResponseEntity<BoardLikeCountResponse> count(
      @PathVariable("boardId") Long boardId
  ) {
    return ResponseEntity.ok(boardLikeService.count(boardId));
  }

  private Long getLoginMemberId(HttpSession session) {
    Object v = session.getAttribute("LOGIN_MEMBER_ID");
    if (v instanceof Long l) return l;
    if (v instanceof Integer i) return i.longValue();
    if (v instanceof String s) {
      try { return Long.parseLong(s); } catch (Exception ignored) {}
    }
    return null;
  }
}
