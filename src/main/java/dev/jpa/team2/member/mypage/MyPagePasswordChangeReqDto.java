package dev.jpa.team2.member.mypage;

public class MyPagePasswordChangeReqDto {
  private String verifyCode;     // 이메일로 받은 6자리 코드
  private String newPassword;
  private String confirmPassword;

  public String getVerifyCode() { return verifyCode; }
  public void setVerifyCode(String verifyCode) { this.verifyCode = verifyCode; }

  public String getNewPassword() { return newPassword; }
  public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

  public String getConfirmPassword() { return confirmPassword; }
  public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
