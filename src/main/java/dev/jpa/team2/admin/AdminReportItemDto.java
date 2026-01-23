package dev.jpa.team2.admin;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReportItemDto {
  private Long reportId;

  private Long reporterId;
  private String reporterNickname;

  private String reasonCode;
  private String reasonText;

  private LocalDateTime createdAt;
}
