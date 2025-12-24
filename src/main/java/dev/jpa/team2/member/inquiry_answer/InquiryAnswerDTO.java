package dev.jpa.team2.member.inquiry_answer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InquiryAnswerDTO {

  private Long inquiryId;
  private Long adminId;
  private String content;

  public InquiryAnswer toEntity() {
    return new InquiryAnswer(inquiryId, adminId, content);
  }
}
