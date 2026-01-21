package dev.jpa.team2.admin_inquiries_reply;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ADMIN_INQUIRIES_REPLY")
@Getter
@Setter
public class AdminInquiryReply {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ADMIN_INQUIRIES_REPLY_ID_GEN")
  @SequenceGenerator(
      name = "SEQ_ADMIN_INQUIRIES_REPLY_ID_GEN",
      sequenceName = "SEQ_ADMIN_INQUIRIES_REPLY_ID",
      allocationSize = 1
  )
  @Column(name = "REPLY_ID")
  private Long replyId;

  @Column(name = "INQUIRY_ID", nullable = false)
  private Long inquiryId;

  @Column(name = "MEMBER_ID", nullable = false)
  private Long memberId; // 답변한 관리자 MEMBER_ID

  @Lob
  @Column(name = "CONTENT", nullable = false)
  private String content;

  @Column(name = "ANSWERED_AT", nullable = false)
  private LocalDateTime answeredAt;

  @PrePersist
  public void prePersist() {
    if (answeredAt == null) answeredAt = LocalDateTime.now();
  }
}
