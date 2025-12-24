package dev.jpa.team2.member.inquiry;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InquiryDTO {

  private Long inquiryId;
  private Long memberId;
  private String title;
  private String content;
  private String category;
  private String status;
  private Date createdAt;

  public Inquiry toEntity() {
    return new Inquiry(memberId, title, content, category);
  }
}
