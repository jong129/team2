package dev.jpa.team2.admin;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminInquiryRowDto {

  private Long inquiryId;
  private Long memberId;

  private String title;
  private String category;   // 선택
  private String status;     // RECEIVED / IN_PROGRESS / CLOSED

  private LocalDateTime createdAt;
}
