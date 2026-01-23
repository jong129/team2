package dev.jpa.team2.admin;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReportBoardDetailDto {

  private Long boardId;
  private String title;
  private String content;

  private Long writerId;
  private String writerNickname;

  private Long categoryId;
  private String categoryName;

  private Long reportCount;
  private LocalDateTime lastReportedAt;

  private List<AdminReportItemDto> reports;
}
