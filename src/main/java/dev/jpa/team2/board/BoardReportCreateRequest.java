package dev.jpa.team2.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardReportCreateRequest {
  private String reasonCode; // 필수 (예: SPAM, ABUSE, ETC)
  private String reasonText; // 선택
}
