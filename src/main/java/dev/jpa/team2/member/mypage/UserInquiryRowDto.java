package dev.jpa.team2.member.mypage;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInquiryRowDto {
  private Long inquiryId;
  private String title;
  private String category;
  private String status;
  private LocalDateTime createdAt;
}
