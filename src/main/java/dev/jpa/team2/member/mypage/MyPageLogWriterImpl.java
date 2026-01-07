package dev.jpa.team2.member.mypage;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class MyPageLogWriterImpl implements MyPageLogWriter {

  // TODO: 아래 3~4개 의존성을 "admin 쪽에서 이미 만든" 서비스/레포로 교체해서 주입
  // 예시:
  // private final MemberUpdateHistoryService memberUpdateHistoryService;
  // private final PasswordChangeHistoryService passwordChangeHistoryService;
  // private final MemberWithdrawHistoryService memberWithdrawHistoryService;
  // private final MemberActivityLogService memberActivityLogService;

  public MyPageLogWriterImpl(
      /* 여기에 admin 쪽 로그 서비스들을 생성자 주입 */
  ) {
    // this.memberUpdateHistoryService = memberUpdateHistoryService;
    // ...
  }

  private String clientIp(HttpServletRequest request) {
    if (request == null) return null;
    String xf = request.getHeader("X-Forwarded-For");
    if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
    return request.getRemoteAddr();
  }

  private String userAgent(HttpServletRequest request) {
    if (request == null) return null;
    return request.getHeader("User-Agent");
  }

  @Override
  public void onNameChanged(Long memberId, String oldName, String newName, HttpServletRequest request) {
    String ip = clientIp(request);
    String ua = userAgent(request);

    // 1) 회원정보수정 이력 INSERT (admin 구현 호출)
    // memberUpdateHistoryService.recordNameChange(memberId, oldName, newName, ip, ua);

    // 2) 회원활동 로그 INSERT
    // memberActivityLogService.record(memberId, "PROFILE_UPDATE", "NAME", ip, ua);
  }

  @Override
  public void onPasswordChanged(Long memberId, HttpServletRequest request) {
    String ip = clientIp(request);
    String ua = userAgent(request);

    // 1) 회원 비번변경 이력 INSERT
    // passwordChangeHistoryService.record(memberId, ip, ua);

    // 2) 회원활동 로그 INSERT
    // memberActivityLogService.record(memberId, "PASSWORD_CHANGE", null, ip, ua);
  }

  @Override
  public void onWithdrawn(Long memberId, String reason, HttpServletRequest request) {
    String ip = clientIp(request);
    String ua = userAgent(request);

    // 1) 회원탈퇴 이력 INSERT
    // memberWithdrawHistoryService.record(memberId, reason, ip, ua);

    // 2) 회원활동 로그 INSERT
    // memberActivityLogService.record(memberId, "WITHDRAW", reason, ip, ua);
  }
}
