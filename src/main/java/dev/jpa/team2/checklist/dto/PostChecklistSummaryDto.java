package dev.jpa.team2.checklist.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostChecklistSummaryDto {

  /** 요약 문장 */
  private String summary;

  /** 완료 후 유지/관리 가이드 */
  private List<String> guides;
}
