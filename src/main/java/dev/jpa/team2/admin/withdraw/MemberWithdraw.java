package dev.jpa.team2.admin.withdraw;

import java.time.LocalDateTime;

import dev.jpa.team2.member.member.Member;
import jakarta.persistence.*;

@Entity
@Table(name = "MEMBER_WITHDRAW")
public class MemberWithdraw {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_WITHDRAW_SEQ")
  @SequenceGenerator(
      name = "MEMBER_WITHDRAW_SEQ",
      sequenceName = "SEQ_MEMBER_WITHDRAW_ID",
      allocationSize = 1
  )
  @Column(name = "WITHDRAW_ID")
  private Long withdrawId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MEMBER_ID", nullable = false)
  private Member member;

  @Column(name = "REASON_CODE", length = 50)
  private String reasonCode;

  @Column(name = "REASON_TEXT", length = 500)
  private String reasonText;

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = LocalDateTime.now();
  }

  public Long getWithdrawId() { return withdrawId; }

  public Member getMember() { return member; }
  public void setMember(Member member) { this.member = member; }

  public String getReasonCode() { return reasonCode; }
  public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

  public String getReasonText() { return reasonText; }
  public void setReasonText(String reasonText) { this.reasonText = reasonText; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
