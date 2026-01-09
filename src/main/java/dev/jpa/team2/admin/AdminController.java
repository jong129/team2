package dev.jpa.team2.admin;

import java.time.LocalDate;

import dev.jpa.team2.member.member_role.MemberRoleService;
import dev.jpa.team2.tool.PageResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  @Autowired
  private MemberRoleService memberRoleService;

  @Autowired
  private AdminService adminService;

  /*
   * =============================== 공통: 관리자 권한 체크 ===============================
   */
  private ResponseEntity<String> checkAdmin(HttpSession session) {
    Object memberIdObj = session.getAttribute("LOGIN_MEMBER_ID");
    if (memberIdObj == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
    }

    Long memberId;
    try {
      memberId = (Long) memberIdObj;
    } catch (ClassCastException e) {
      memberId = Long.valueOf(String.valueOf(memberIdObj));
    }

    boolean isAdmin = memberRoleService.isAdmin(memberId);
    if (!isAdmin) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
    }

    return null; // 통과
  }

  @GetMapping("/dashboard")
  public ResponseEntity<?> dashboard(HttpSession session) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    return ResponseEntity.ok("ADMIN DASHBOARD OK");
  }

  // 회원조회
  @GetMapping("/members")
  public ResponseEntity<?> members(
      HttpSession session,
      @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    PageResponse<AdminMemberListDto> res = adminService.getMembers(keyword, pageable);
    return ResponseEntity.ok(res);
  }

  // ✅ 로그인이력: 검색 + 페이징 + 기간(from/to)
  // 호출 예: /api/admin/login-histories?keyword=&from=2026-01-04&to=2026-01-05&page=0&size=10
  @GetMapping("/login-histories")
  public ResponseEntity<?> loginHistories(
      HttpSession session,
      @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    PageResponse<AdminLoginHistoryRowDto> res =
        adminService.getLoginHistories(keyword, from, to, pageable);

    return ResponseEntity.ok(res);
  }

  // ✅ 기간 삭제
  // 호출 예: POST /api/admin/login-histories/purge?from=2026-01-04&to=2026-01-05
  @PostMapping("/login-histories/purge")
  public ResponseEntity<?> purgeLoginHistories(
      HttpSession session,
      @RequestParam(name = "from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(name = "to") @DateTimeFormat(iso = ISO.DATE) LocalDate to
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    int deleted = adminService.purgeLoginHistories(from, to);
    return ResponseEntity.ok(deleted); // 삭제 건수 반환
  }

  // ✅ 단일 삭제
  // 호출 예: DELETE /api/admin/login-histories/10
  @DeleteMapping("/login-histories/{historyId}")
  public ResponseEntity<?> deleteLoginHistory(
      HttpSession session,
      @PathVariable("historyId") Long historyId
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    adminService.deleteLoginHistory(historyId);
       return ResponseEntity.ok().build();
  }
}
