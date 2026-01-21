package dev.jpa.team2.member.mypage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInquiryCreateRequest {
  private String title;
  private String content;
  private String category; // 선택
}
