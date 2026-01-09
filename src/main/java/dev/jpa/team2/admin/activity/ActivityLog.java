package dev.jpa.team2.admin.activity;

import java.time.LocalDateTime;

import dev.jpa.team2.member.member.Member;
import jakarta.persistence.*;

@Entity
@Table(name = "ACTIVITY_LOG")
public class ActivityLog {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACTIVITY_LOG_SEQ")
  @SequenceGenerator(
      name = "ACTIVITY_LOG_SEQ",
      sequenceName = "SEQ_ACTIVITY_LOG_ID",
      allocationSize = 1
  )
  @Column(name = "LOG_ID")
  private Long logId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MEMBER_ID", nullable = false)
  private Member member;

  @Column(name = "ACTION_TYPE", nullable = false, length = 100)
  private String actionType;

  @Column(name = "ACTION_DETAIL", length = 500)
  private String actionDetail;

  @Column(name = "ACTION_AT", nullable = false)
  private LocalDateTime actionAt;

  @Column(name = "ACTION_IP", length = 50)
  private String actionIp;

  @Column(name = "USER_AGENT", length = 300)
  private String userAgent;

  @PrePersist
  void prePersist() {
    if (actionAt == null) actionAt = LocalDateTime.now();
  }

  public Long getLogId() { return logId; }

  public Member getMember() { return member; }
  public void setMember(Member member) { this.member = member; }

  public String getActionType() { return actionType; }
  public void setActionType(String actionType) { this.actionType = actionType; }

  public String getActionDetail() { return actionDetail; }
  public void setActionDetail(String actionDetail) { this.actionDetail = actionDetail; }

  public LocalDateTime getActionAt() { return actionAt; }
  public void setActionAt(LocalDateTime actionAt) { this.actionAt = actionAt; }

  public String getActionIp() { return actionIp; }
  public void setActionIp(String actionIp) { this.actionIp = actionIp; }

  public String getUserAgent() { return userAgent; }
  public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
