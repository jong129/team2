package dev.jpa.team2.board.category;

import dev.jpa.team2.member.member_role.MemberRoleService;
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
  private final MemberRoleService memberRoleService;

  @GetMapping("/list")
  public ResponseEntity<List<BoardCategoryDto>> list(HttpSession session) {
    requireAdmin(session);
    return ResponseEntity.ok(boardCategoryService.adminList());
  }

  @PostMapping
  public ResponseEntity<BoardCategoryDto> create(HttpSession session,
                                                @RequestBody BoardCategoryCreateRequest req) {
    requireAdmin(session);
    return ResponseEntity.ok(boardCategoryService.create(req));
  }

  @PutMapping("/{categoryId}")
  public ResponseEntity<BoardCategoryDto> update(HttpSession session,
                                                @PathVariable("categoryId") Long categoryId,
                                                @RequestBody BoardCategoryUpdateRequest req) {
    requireAdmin(session);
    return ResponseEntity.ok(boardCategoryService.update(categoryId, req));
  }

  @DeleteMapping("/{categoryId}")
  public ResponseEntity<Void> delete(HttpSession session,
                                     @PathVariable("categoryId") Long categoryId) {
    requireAdmin(session);
    boardCategoryService.delete(categoryId);
    return ResponseEntity.ok().build();
  }

  private void requireAdmin(HttpSession session) {
    Long memberId = getLoginMemberId(session);
    if (memberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");

    // 1) DB 기반 권한 체크(네 프로젝트에서 이미 쓰는 방식)
    boolean isAdmin = memberRoleService.isAdmin(memberId);
    if (!isAdmin) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin only");

    // 2) (선택) 세션 roles까지 같이 쓰고 싶으면 아래처럼도 가능
    // List<String> roles = getLoginRoles(session);
    // if (roles == null || !roles.contains("ADMIN")) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin only");
  }

  private Long getLoginMemberId(HttpSession session) {
    Object v = session.getAttribute("LOGIN_MEMBER_ID"); // ✅ 로그인 컨트롤러와 동일 키
    if (v instanceof Long l) return l;
    if (v instanceof Integer i) return i.longValue();
    if (v instanceof String s) {
      try { return Long.parseLong(s); } catch (Exception ignored) {}
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private List<String> getLoginRoles(HttpSession session) {
    Object v = session.getAttribute("LOGIN_ROLES"); // ✅ 로그인 컨트롤러와 동일 키
    if (v instanceof List<?> list) {
      return (List<String>) list;
    }
    return null;
  }
}

