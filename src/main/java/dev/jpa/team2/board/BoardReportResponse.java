package dev.jpa.team2.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardReportResponse {
  private Long reportId;
  private String message;

  public static BoardReportResponse ok(Long reportId) {
    BoardReportResponse r = new BoardReportResponse();
    r.setReportId(reportId);
    r.setMessage("reported");
    return r;
  }
}
