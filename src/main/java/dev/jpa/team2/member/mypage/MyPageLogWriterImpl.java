package dev.jpa.team2.member.mypage;

import org.springframework.stereotype.Component;

import dev.jpa.team2.admin.activity.ActivityLogService;
import dev.jpa.team2.admin.update.MemberUpdateHistoryService;
import dev.jpa.team2.admin.password.PasswordChangeHistoryService;
import dev.jpa.team2.admin.withdraw.MemberWithdrawService;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class MyPageLogWriterImpl implements MyPageLogWriter {

  private final MemberUpdateHistoryService memberUpdateHistoryService;
  private final PasswordChangeHistoryService passwordChangeHistoryService;
  private final MemberWithdrawService memberWithdrawService;
  private final ActivityLogService activityLogService;

  public MyPageLogWriterImpl(MemberUpdateHistoryService memberUpdateHistoryService,
                             PasswordChangeHistoryService passwordChangeHistoryService,
                             MemberWithdrawService memberWithdrawService,
                             ActivityLogService activityLogService) {
    this.memberUpdateHistoryService = memberUpdateHistoryService;
    this.passwordChangeHistoryService = passwordChangeHistoryService;
    this.memberWithdrawService = memberWithdrawService;
    this.activityLogService = activityLogService;
  }

  private String safe(String s) {
    return (s == null) ? null : s.trim();
  }

  @Override
  public void onNameChanged(Long memberId, String oldName, String newName, HttpServletRequest request) {
    if (memberId == null) return;

    String oldV = safe(oldName);
    String newV = safe(newName);

    // 1) 회원정보 수정 이력 (본인 수정: changedById = memberId)
    memberUpdateHistoryService.recordNameChange(
        memberId,
        memberId,
        oldV,
        newV,
        "USER_CHANGE"
    );

    // 2) 활동 로그
    // 디테일은 너무 길게 가지 말고, 필요하면 "NAME" 정도로만
    activityLogService.record(memberId, "PROFILE_UPDATE", "NAME", request);
  }

  @Override
  public void onPasswordChanged(Long memberId, HttpServletRequest request) {
    if (memberId == null) return;

    // 1) 비번 변경 이력 (본인 변경)
    passwordChangeHistoryService.recordSelf(memberId);

    // 2) 활동 로그
    activityLogService.record(memberId, "PASSWORD_CHANGE", null, request);
  }

  @Override
  public void onWithdrawn(Long memberId, String reason, HttpServletRequest request) {
    if (memberId == null) return;

    String reasonText = safe(reason);

    // 1) 탈퇴 이력
    // reasonCode가 따로 없으니 null로 두고, reasonText에 저장
    memberWithdrawService.record(memberId, null, reasonText);

    // 2) 활동 로그
    activityLogService.record(memberId, "WITHDRAW", reasonText, request);
  }
}

