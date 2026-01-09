package dev.jpa.team2.member.mypage;

import jakarta.servlet.http.HttpServletRequest;

public interface MyPageLogWriter {

  // 회원정보수정 이력 + 회원활동 로그
  void onNameChanged(Long memberId, String oldName, String newName, HttpServletRequest request);

  // 회원 비번변경 이력 + 회원활동 로그
  void onPasswordChanged(Long memberId, HttpServletRequest request);

  // 회원탈퇴 이력 + 회원활동 로그
  void onWithdrawn(Long memberId, String reason, HttpServletRequest request);
}
