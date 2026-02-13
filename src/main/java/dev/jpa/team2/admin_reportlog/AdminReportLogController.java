package dev.jpa.team2.admin_reportlog;

import dev.jpa.team2.admin.AdminReportBoardDetailDto;
import dev.jpa.team2.admin.AdminReportBoardRowDto;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reports")
public class AdminReportLogController {

  private final AdminReportLogService adminReportLogService;

  @GetMapping("/boards")
  public ResponseEntity<Page<AdminReportBoardRowDto>> searchBoards(
      @RequestParam(name = "keyword", required = false) String keyword,
      @RequestParam(name = "categoryId", required = false) Long categoryId,
      @RequestParam(name = "minCount", required = false) Long minCount,
      @RequestParam(name = "fromAt", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromAt,
      @RequestParam(name = "toExclusive", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toExclusive,
      Pageable pageable,
      HttpSession session
  ) {
    Long adminId = getLoginMemberId(session);
    return ResponseEntity.ok(
        adminReportLogService.searchBoards(adminId, keyword, categoryId, minCount, fromAt, toExclusive, pageable)
    );
  }

  @GetMapping("/boards/{boardId}")
  public ResponseEntity<AdminReportBoardDetailDto> detail(
      @PathVariable(name = "boardId") Long boardId,
      HttpSession session
  ) {
    Long adminId = getLoginMemberId(session);
    return ResponseEntity.ok(adminReportLogService.detail(adminId, boardId));
  }

  @DeleteMapping("/boards/{boardId}")
  public ResponseEntity<Void> deleteHard(
      @PathVariable(name = "boardId") Long boardId,
      HttpSession session
  ) {
    Long adminId = getLoginMemberId(session);
    adminReportLogService.deleteHard(adminId, boardId);
    return ResponseEntity.noContent().build();
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

