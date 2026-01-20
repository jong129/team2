package dev.jpa.team2.admin;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminInquiryDetailDto {

  // 문의 본문
  private Long inquiryId;
  private Long memberId;

  private String title;
  private String content;

  private String category;   // 선택
  private String status;     // RECEIVED / IN_PROGRESS / CLOSED
  private LocalDateTime createdAt;

  // 답변(있으면 내려줌, 없으면 null)
  private Long replyId;
  private Long adminMemberId;
  private String replyContent;
  private LocalDateTime answeredAt;
}
