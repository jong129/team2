package dev.jpa.team2.board.report;

import dev.jpa.team2.board.BoardReportCreateRequest;
import dev.jpa.team2.board.BoardReportResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardReportController {

  private final BoardReportService boardReportService;

  @PostMapping("/posts/{boardId}/reports")
  public ResponseEntity<BoardReportResponse> create(
      @PathVariable("boardId") Long boardId,
      @RequestBody BoardReportCreateRequest req,
      HttpSession session
  ) {
    Long memberId = getLoginMemberId(session);
    return ResponseEntity.ok(boardReportService.create(boardId, memberId, req));
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
