package dev.jpa.team2.admin_inquiries_reply;

import dev.jpa.team2.admin.AdminInquiryDetailDto;
import dev.jpa.team2.admin.AdminInquiryReplyRequest;
import dev.jpa.team2.admin.AdminInquiryRowDto;
import dev.jpa.team2.member.member_role.MemberRoleService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

  private final MemberRoleService memberRoleService;
  private final AdminInquiryService adminInquiryService;

  private Long loginMemberId(HttpSession session) {
    Object v = session.getAttribute("LOGIN_MEMBER_ID");
    if (v == null) return null;
    if (v instanceof Long) return (Long) v;
    return Long.valueOf(String.valueOf(v));
  }

  private void requireAdmin(HttpSession session) {
    Long memberId = loginMemberId(session);
    if (memberId == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."
      );
    }
    if (!memberRoleService.isAdmin(memberId)) {
      throw new org.springframework.web.server.ResponseStatusException(
          HttpStatus.FORBIDDEN, "관리자 권한이 없습니다."
      );
    }
  }

  // 1) 관리자 문의 목록
  // GET /api/admin/inquiries?status=RECEIVED&page=0&size=10
  @GetMapping
  public ResponseEntity<Page<AdminInquiryRowDto>> list(
      HttpSession session,
      @RequestParam(name = "status", required = false) String status,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    requireAdmin(session);
    return ResponseEntity.ok(adminInquiryService.list(status, pageable));
  }

  // 2) 관리자 문의 상세(+ RECEIVED면 IN_PROGRESS로 자동 변경)
  @GetMapping("/{inquiryId}")
  public ResponseEntity<AdminInquiryDetailDto> detail(
      HttpSession session,
      @PathVariable(name = "inquiryId") Long inquiryId
  ) {
    requireAdmin(session);
    return ResponseEntity.ok(adminInquiryService.detailAndMarkInProgress(inquiryId));
  }

  // 3) 답변 등록(등록 성공 시 문의 상태 CLOSED)
  @PostMapping("/reply")
  public ResponseEntity<?> reply(
      HttpSession session,
      @RequestBody AdminInquiryReplyRequest req
  ) {
    requireAdmin(session);

    Long adminMemberId = loginMemberId(session);
    Long replyId = adminInquiryService.reply(adminMemberId, req);

    return ResponseEntity.ok(Map.of(
        "success", true,
        "replyId", replyId
    ));
  }
}

