package dev.jpa.team2.board.comment;

import dev.jpa.team2.board.BoardCommentCreateRequest;
import dev.jpa.team2.board.BoardCommentDto;
import dev.jpa.team2.board.BoardCommentUpdateRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardCommentController {

  private final BoardCommentService boardCommentService;

  @GetMapping("/posts/{boardId}/comments")
  public ResponseEntity<List<BoardCommentDto>> list(@PathVariable("boardId") Long boardId) {
    return ResponseEntity.ok(boardCommentService.list(boardId));
  }

  @PostMapping("/posts/{boardId}/comments")
  public ResponseEntity<BoardCommentDto> create(
      @PathVariable("boardId") Long boardId,
      @RequestBody BoardCommentCreateRequest req,
      HttpSession session
  ) {
    Long memberId = getLoginMemberId(session);
    return ResponseEntity.ok(boardCommentService.create(boardId, memberId, req));
  }

  @PutMapping("/comments/{commentId}")
  public ResponseEntity<BoardCommentDto> update(
      @PathVariable("commentId") Long commentId,
      @RequestBody BoardCommentUpdateRequest req,
      HttpSession session
  ) {
    Long memberId = getLoginMemberId(session);
    return ResponseEntity.ok(boardCommentService.update(commentId, memberId, req));
  }

  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> delete(
      @PathVariable("commentId") Long commentId,
      HttpSession session
  ) {
    Long memberId = getLoginMemberId(session);
    boardCommentService.delete(commentId, memberId);
    return ResponseEntity.ok().build();
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

