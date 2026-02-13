package dev.jpa.team2.admin;

import java.time.LocalDateTime;

public interface AdminReportBoardRowDto {
  Long getBoardId();

  String getTitle();
  Long getWriterId();
  String getWriterNickname();

  Long getCategoryId();
  String getCategoryName();

  Long getReportCount();
  LocalDateTime getLastReportedAt();
}
