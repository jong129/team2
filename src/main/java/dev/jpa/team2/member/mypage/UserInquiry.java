package dev.jpa.team2.member.mypage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "USER_INQUIRIES")
@Getter
@Setter
public class UserInquiry {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_USER_INQUIRIES_ID_GEN")
  @SequenceGenerator(
      name = "SEQ_USER_INQUIRIES_ID_GEN",
      sequenceName = "SEQ_USER_INQUIRIES_ID",
      allocationSize = 1
  )
  @Column(name = "INQUIRY_ID")
  private Long inquiryId;

  @Column(name = "MEMBER_ID", nullable = false)
  private Long memberId;

  @Column(name = "TITLE", nullable = false, length = 255)
  private String title;

  @Lob
  @Column(name = "CONTENT", nullable = false)
  private String content;

  @Column(name = "CATEGORY", length = 100)
  private String category; // 선택

  @Column(name = "STATUS", nullable = false, length = 20)
  private String status; // RECEIVED / IN_PROGRESS / CLOSED

  @Column(name = "CREATED_AT", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (this.status == null) this.status = "RECEIVED";
    if (this.createdAt == null) this.createdAt = LocalDateTime.now();
  }
}
