package dev.jpa.team2.member.inquiry_answer;

import java.util.Date;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@SequenceGenerator(
    name = "answer_seq",
    sequenceName = "SEQ_INQUIRY_ANSWER_ID",
    allocationSize = 1
)
public class InquiryAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "answer_seq")
  @Column(name = "ANSWER_ID")
  private Long answerId;

  @Column(name = "INQUIRY_ID", nullable = false)
  private Long inquiryId;

  @Column(name = "ADMIN_ID", nullable = false)
  private Long adminId;

  @Lob
  @Column(name = "CONTENT", nullable = false)
  private String content;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_AT")
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "UPDATED_AT")
  private Date updatedAt;

  public InquiryAnswer() {}

  public InquiryAnswer(Long inquiryId, Long adminId, String content) {
    this.inquiryId = inquiryId;
    this.adminId = adminId;
    this.content = content;
    this.createdAt = new Date();
  }
}
