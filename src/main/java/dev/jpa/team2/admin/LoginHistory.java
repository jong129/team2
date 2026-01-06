package dev.jpa.team2.admin;

import java.time.LocalDateTime;

import dev.jpa.team2.member.member.Member;
import jakarta.persistence.*;

@Entity
@Table(name = "LOGIN_HISTORY")
public class LoginHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOGIN_HISTORY_SEQ")
  @SequenceGenerator(
      name = "LOGIN_HISTORY_SEQ",
      sequenceName = "SEQ_LOGIN_HISTORY_ID",
      allocationSize = 1
  )
  @Column(name = "HISTORY_ID")
  private Long historyId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MEMBER_ID", nullable = false)
  private Member member;

  @Column(name = "LOGIN_AT", nullable = false)
  private LocalDateTime loginAt;

  @Column(name = "LOGIN_IP", length = 50)
  private String loginIp;

  @Column(name = "USER_AGENT", length = 300)
  private String userAgent;

  @Column(name = "SUCCESS_YN", nullable = false, length = 1)
  private String successYn; // 'Y' / 'N'

  @PrePersist
  void prePersist() {
    if (loginAt == null) loginAt = LocalDateTime.now();
    if (successYn == null) successYn = "Y";
  }

  public Long getHistoryId() { return historyId; }

  public Member getMember() { return member; }
  public void setMember(Member member) { this.member = member; }

  public LocalDateTime getLoginAt() { return loginAt; }
  public void setLoginAt(LocalDateTime loginAt) { this.loginAt = loginAt; }

  public String getLoginIp() { return loginIp; }
  public void setLoginIp(String loginIp) { this.loginIp = loginIp; }

  public String getUserAgent() { return userAgent; }
  public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

  public String getSuccessYn() { return successYn; }
  public void setSuccessYn(String successYn) { this.successYn = successYn; }
}
