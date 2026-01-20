package dev.jpa.team2.member.mypage;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInquiryDetailResDto {

  private Long inquiryId;
  private String title;
  private String content;
  private String category;
  private String status;
  private LocalDateTime createdAt;

  // 답변(없으면 null)
  private Long replyId;
  private String replyContent;
  private LocalDateTime answeredAt;
}
