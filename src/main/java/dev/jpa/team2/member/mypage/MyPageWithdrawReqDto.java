package dev.jpa.team2.member.mypage;

public class MyPageWithdrawReqDto {
  private String password;
  private String reason; // 선택

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
}
