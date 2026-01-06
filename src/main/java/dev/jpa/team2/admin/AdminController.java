package dev.jpa.team2.admin;

import dev.jpa.team2.member.member_role.MemberRoleService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  @Autowired
  private MemberRoleService memberRoleService;

  @GetMapping("/dashboard")
  public ResponseEntity<?> dashboard(HttpSession session) {

    Object memberIdObj = session.getAttribute("LOGIN_MEMBER_ID");
    if (memberIdObj == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("로그인이 필요합니다.");
    }

    Long memberId;
    try {
      memberId = (Long) memberIdObj;
    } catch (ClassCastException e) {
      // 혹시 String으로 저장된 경우까지 방어
      memberId = Long.valueOf(String.valueOf(memberIdObj));
    }

    boolean isAdmin = memberRoleService.isAdmin(memberId);
    if (!isAdmin) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("관리자만 접근 가능합니다.");
    }

    return ResponseEntity.ok("ADMIN DASHBOARD OK");
  }
}

