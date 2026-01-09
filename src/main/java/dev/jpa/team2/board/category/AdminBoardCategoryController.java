package dev.jpa.team2.board.category;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/board/categories")
public class AdminBoardCategoryController {

  private final BoardCategoryService boardCategoryService;

  @GetMapping("/list")
  public ResponseEntity<List<BoardCategoryDto>> list(HttpSession session) {
    requireAdmin(session);
    return ResponseEntity.ok(boardCategoryService.adminList());
  }

  @PostMapping
  public ResponseEntity<BoardCategoryDto> create(HttpSession session, @RequestBody BoardCategoryCreateRequest req) {
    requireAdmin(session);
    return ResponseEntity.ok(boardCategoryService.create(req));
  }

  @PutMapping("/{categoryId}")
  public ResponseEntity<BoardCategoryDto> update(HttpSession session, @PathVariable("categoryId") Long categoryId,
      @RequestBody BoardCategoryUpdateRequest req) {
    requireAdmin(session);
    return ResponseEntity.ok(boardCategoryService.update(categoryId, req));
  }

  @DeleteMapping("/{categoryId}")
  public ResponseEntity<Void> delete(HttpSession session, @PathVariable("categoryId") Long categoryId) {
    requireAdmin(session);
    boardCategoryService.delete(categoryId);
    return ResponseEntity.ok().build();
  }

  private void requireAdmin(HttpSession session) {
    Long memberId = getLoginMemberId(session);
    if (memberId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
    }

    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) session.getAttribute("LOGIN_ROLES");
    if (roles == null || !roles.contains("ADMIN")) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin only");
    }
  }

  private Long getLoginMemberId(HttpSession session) {
    Object v = session.getAttribute("LOGIN_MEMBER_ID");
    if (v instanceof Long l)
      return l;
    if (v instanceof Integer i)
      return i.longValue();
    return null;
  }
}
