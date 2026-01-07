package dev.jpa.team2.admin.update;

import java.time.LocalDateTime;

import dev.jpa.team2.member.member.Member;
import jakarta.persistence.*;

@Entity
@Table(name = "MEMBER_UPDATE_HISTORY")
public class MemberUpdateHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_UPDATE_HISTORY_SEQ")
  @SequenceGenerator(
      name = "MEMBER_UPDATE_HISTORY_SEQ",
      sequenceName = "SEQ_MEMBER_UPDATE_HISTORY_ID",
      allocationSize = 1
  )
  @Column(name = "HISTORY_ID")
  private Long historyId;

  // 수정 대상 회원
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MEMBER_ID", nullable = false)
  private Member member;

  // 수정한 사람(본인수정이면 memberId와 동일, 관리자면 admin의 memberId)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "CHANGED_BY", nullable = false)
  private Member changedBy;

  @Column(name = "FIELD_NAME", nullable = false, length = 100)
  private String fieldName;

  @Column(name = "OLD_VALUE", length = 500)
  private String oldValue;

  @Column(name = "NEW_VALUE", length = 500)
  private String newValue;

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

  public Long getHistoryId() { return historyId; }

  public Member getMember() { return member; }
  public void setMember(Member member) { this.member = member; }

  public Member getChangedBy() { return changedBy; }
  public void setChangedBy(Member changedBy) { this.changedBy = changedBy; }

  public String getFieldName() { return fieldName; }
  public void setFieldName(String fieldName) { this.fieldName = fieldName; }

  public String getOldValue() { return oldValue; }
  public void setOldValue(String oldValue) { this.oldValue = oldValue; }

  public String getNewValue() { return newValue; }
  public void setNewValue(String newValue) { this.newValue = newValue; }

  public String getChangeType() { return changeType; }
  public void setChangeType(String changeType) { this.changeType = changeType; }

  public LocalDateTime getChangedAt() { return changedAt; }
  public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
