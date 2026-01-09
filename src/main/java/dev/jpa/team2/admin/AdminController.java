package dev.jpa.team2.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import dev.jpa.team2.admin.AdminService.AdminMemberService;
import dev.jpa.team2.admin.activity.ActivityLogService;
import dev.jpa.team2.admin.password.PasswordChangeHistoryService;
import dev.jpa.team2.admin.update.MemberUpdateHistoryService;
import dev.jpa.team2.admin.withdraw.MemberWithdrawService;
import dev.jpa.team2.member.member_role.MemberRoleService;
import dev.jpa.team2.tool.PageResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

  // ✅ 추가: 로그 서비스 4개
  @Autowired
  private ActivityLogService activityLogService;

  @Autowired
  private MemberUpdateHistoryService memberUpdateHistoryService;

  @Autowired
  private PasswordChangeHistoryService passwordChangeHistoryService;

  @Autowired
  private MemberWithdrawService memberWithdrawService;

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

  // ✅ 날짜 필터용 헬퍼 (to는 inclusive로 받고, 내부에서는 toExclusive로 변환)
  private LocalDateTime toFromAt(LocalDate from) {
    return (from == null) ? null : from.atStartOfDay();
  }

  private LocalDateTime toExclusive(LocalDate to) {
    return (to == null) ? null : to.plusDays(1).atStartOfDay();
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

  /* =====================================================================
   * ✅ 추가 1) 활동 로그 (ACTIVITY_LOG)
   * ===================================================================== */

  // 호출 예: /api/admin/activity-logs?keyword=&actionType=&from=2026-01-04&to=2026-01-05&page=0&size=10
  @GetMapping("/activity-logs")
  public ResponseEntity<?> activityLogs(
      HttpSession session,
      @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
      @RequestParam(name = "actionType", required = false, defaultValue = "") String actionType,
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    Page<AdminActivityLogRowDto> res = activityLogService.search(
        keyword,
        actionType,
        toFromAt(from),
        toExclusive(to),
        pageable
    );

    return ResponseEntity.ok(res);
  }

  // 호출 예: POST /api/admin/activity-logs/purge?from=2026-01-01&to=2026-01-05&actionType=PASSWORD_CHANGE
  @PostMapping("/activity-logs/purge")
  public ResponseEntity<?> purgeActivityLogs(
      HttpSession session,
      @RequestParam(name = "from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(name = "to") @DateTimeFormat(iso = ISO.DATE) LocalDate to,
      @RequestParam(name = "actionType", required = false, defaultValue = "") String actionType
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    int deleted = activityLogService.purgeByPeriod(actionType, toFromAt(from), toExclusive(to));
    return ResponseEntity.ok(deleted);
  }

  /* =====================================================================
   * ✅ 추가 2) 회원정보 수정 이력 (MEMBER_UPDATE_HISTORY)
   * ===================================================================== */

  // 호출 예: /api/admin/member-update-histories?keyword=&fieldName=NAME&changeType=USER_CHANGE&from=2026-01-04&to=2026-01-05&page=0&size=10
  @GetMapping("/member-update-histories")
  public ResponseEntity<?> memberUpdateHistories(
      HttpSession session,
      @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
      @RequestParam(name = "fieldName", required = false, defaultValue = "") String fieldName,
      @RequestParam(name = "changeType", required = false, defaultValue = "") String changeType,
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    Page<AdminMemberUpdateHistoryRowDto> res = memberUpdateHistoryService.search(
        keyword,
        fieldName,
        changeType,
        toFromAt(from),
        toExclusive(to),
        pageable
    );

    return ResponseEntity.ok(res);
  }

  @PostMapping("/member-update-histories/purge")
  public ResponseEntity<?> purgeMemberUpdateHistories(
      HttpSession session,
      @RequestParam(name = "from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(name = "to") @DateTimeFormat(iso = ISO.DATE) LocalDate to
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    int deleted = memberUpdateHistoryService.purgeByPeriod(toFromAt(from), toExclusive(to));
    return ResponseEntity.ok(deleted);
  }

  /* =====================================================================
   * ✅ 추가 3) 비밀번호 변경 이력 (PASSWORD_CHANGE_HISTORY)
   * ===================================================================== */

  // 호출 예: /api/admin/password-change-histories?keyword=&changeType=USER_CHANGE&from=2026-01-04&to=2026-01-05&page=0&size=10
  @GetMapping("/password-change-histories")
  public ResponseEntity<?> passwordChangeHistories(
      HttpSession session,
      @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
      @RequestParam(name = "changeType", required = false, defaultValue = "") String changeType,
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    Page<AdminPasswordChangeHistoryRowDto> res = passwordChangeHistoryService.search(
        keyword,
        changeType,
        toFromAt(from),
        toExclusive(to),
        pageable
    );

    return ResponseEntity.ok(res);
  }

  @PostMapping("/password-change-histories/purge")
  public ResponseEntity<?> purgePasswordChangeHistories(
      HttpSession session,
      @RequestParam(name = "from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(name = "to") @DateTimeFormat(iso = ISO.DATE) LocalDate to
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    int deleted = passwordChangeHistoryService.purgeByPeriod(toFromAt(from), toExclusive(to));
    return ResponseEntity.ok(deleted);
  }

  /* =====================================================================
   * ✅ 추가 4) 회원탈퇴 이력 (MEMBER_WITHDRAW)
   * ===================================================================== */

  // 호출 예: /api/admin/withdraw-histories?keyword=&from=2026-01-04&to=2026-01-05&page=0&size=10
  @GetMapping("/withdraw-histories")
  public ResponseEntity<?> withdrawHistories(
      HttpSession session,
      @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    Page<AdminMemberWithdrawRowDto> res = memberWithdrawService.search(
        keyword,
        toFromAt(from),
        toExclusive(to),
        pageable
    );

    return ResponseEntity.ok(res);
  }

  @PostMapping("/withdraw-histories/purge")
  public ResponseEntity<?> purgeWithdrawHistories(
      HttpSession session,
      @RequestParam(name = "from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(name = "to") @DateTimeFormat(iso = ISO.DATE) LocalDate to
  ) {
    ResponseEntity<String> denied = checkAdmin(session);
    if (denied != null) return denied;

    int deleted = memberWithdrawService.purgeByPeriod(toFromAt(from), toExclusive(to));
    return ResponseEntity.ok(deleted);
  }
  @RestController
  @RequestMapping("/api/admin")
  public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    public AdminMemberController(AdminMemberService adminMemberService) {
      this.adminMemberService = adminMemberService;
    }

    @PostMapping("/members/{memberId}/restore")
    public ResponseEntity<?> restore(
        @PathVariable("memberId") Long memberId,
        HttpSession session
    ) {
      // 관리자 권한 체크(네 AdminController의 checkAdmin 재사용 불가하니 여기서 간단히)
      Object loginMemberId = session.getAttribute("LOGIN_MEMBER_ID");
      if (loginMemberId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("success", false, "message", "로그인이 필요합니다."));
      }

      // memberRoleService를 여기서 쓰려면 외부 클래스 필드 접근이 애매할 수 있어
      // 그래서 AdminMemberService 내부에서 권한체크를 하거나,
      // 최소한 여기서는 기존 세션 roles로 체크하는 방식으로 가는 게 안전함.
      Object rolesObj = session.getAttribute("LOGIN_ROLES");
      if (rolesObj == null) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("success", false, "message", "관리자 권한이 없습니다."));
      }

      @SuppressWarnings("unchecked")
      java.util.List<String> roles = (java.util.List<String>) rolesObj;

      if (!roles.contains("ADMIN")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("success", false, "message", "관리자 권한이 없습니다."));
      }

      adminMemberService.restoreMember(memberId);
      return ResponseEntity.ok(Map.of("success", true, "message", "복구 완료"));
    }

  }
}
