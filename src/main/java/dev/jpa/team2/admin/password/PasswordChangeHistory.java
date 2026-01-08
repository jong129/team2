package dev.jpa.team2.admin.password;

import java.time.LocalDateTime;

import dev.jpa.team2.member.member.Member;
import jakarta.persistence.*;

@Entity
@Table(name = "PASSWORD_CHANGE_HISTORY")
public class PasswordChangeHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PASSWORD_CHANGE_HISTORY_SEQ")
  @SequenceGenerator(
      name = "PASSWORD_CHANGE_HISTORY_SEQ",
      sequenceName = "SEQ_PASSWORD_CHANGE_HISTORY_ID",
      allocationSize = 1
  )
  @Column(name = "CHANGE_ID")
  private Long changeId;

  // 변경 대상 회원
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MEMBER_ID", nullable = false)
  private Member member;

  // 변경 수행자(본인 변경이면 memberId 동일, 관리자 변경이면 admin memberId)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "CHANGED_BY", nullable = false)
  private Member changedBy;

  // ERD에 있어도 실제 저장은 null/마스킹 권장
  @Column(name = "OLD_PASSWORD", length = 255)
  private String oldPassword;

  // USER_CHANGE / ADMIN_CHANGE 등
  @Column(name = "CHANGE_TYPE", nullable = false, length = 20)
  private String changeType;

  @Column(name = "CHANGED_AT", nullable = false)
  private LocalDateTime changedAt;

  @PrePersist
  void prePersist() {
    if (changedAt == null) changedAt = LocalDateTime.now();
    if (changeType == null) changeType = "USER_CHANGE";
  }

  public Long getChangeId() { return changeId; }

  public Member getMember() { return member; }
  public void setMember(Member member) { this.member = member; }

  public Member getChangedBy() { return changedBy; }
  public void setChangedBy(Member changedBy) { this.changedBy = changedBy; }

  public String getOldPassword() { return oldPassword; }
  public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

  public String getChangeType() { return changeType; }
  public void setChangeType(String changeType) { this.changeType = changeType; }

  public LocalDateTime getChangedAt() { return changedAt; }
  public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
