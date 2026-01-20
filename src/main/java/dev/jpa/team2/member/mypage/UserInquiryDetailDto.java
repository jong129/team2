package dev.jpa.team2.member.mypage;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInquiryDetailDto {
  private Long inquiryId;
  private Long memberId;
  private String title;
  private String content;
  private String category;
  private String status;
  private LocalDateTime createdAt;
  private Long replyId;
  private String replyContent;
  private LocalDateTime answeredAt;
}
