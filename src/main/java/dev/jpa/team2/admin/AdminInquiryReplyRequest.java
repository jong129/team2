package dev.jpa.team2.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminInquiryReplyRequest {

  private Long inquiryId;  // 어떤 문의에 답변할지
  private String content;  // 답변 내용
}
