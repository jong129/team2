package dev.jpa.team2.member.inquiry;

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
    name = "inquiry_seq",
    sequenceName = "SEQ_INQUIRY_ID",
    allocationSize = 1
)
public class Inquiry {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inquiry_seq")
  @Column(name = "INQUIRY_ID")
  private Long inquiryId;

  @Column(name = "MEMBER_ID", nullable = false)
  private Long memberId;

  @Column(name = "TITLE", nullable = false)
  private String title;

  @Lob
  @Column(name = "CONTENT", nullable = false)
  private String content;

  @Column(name = "CATEGORY")
  private String category;

  @Column(name = "STATUS")
  private String status;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_AT")
  private Date createdAt;

  public Inquiry() {}

  /** 사용자 문의 등록용 */
  public Inquiry(Long memberId, String title, String content, String category) {
    this.memberId = memberId;
    this.title = title;
    this.content = content;
    this.category = category;
    this.status = "RECEIVED";
    this.createdAt = new Date();
  }
}
