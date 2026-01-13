package dev.jpa.team2.checklist.ai;

import static dev.jpa.team2.checklist.ai.PostChecklistSignalType.*;

public class PostChecklistSignalRule {

  public static AiPostItemSignalDTO judge(AiPostItemStatDTO stat) {

    double doneRate = nzd(stat.getDoneRate());
    double notDoneRate = nzd(stat.getNotDoneRate());
    double notRequiredRate = nzd(stat.getNotRequiredRate());

    Double delta = stat.getDeltaRating();
    boolean required = "Y".equalsIgnoreCase(stat.getRequiredYn());

    // 0) 표본이 너무 적으면 과한 판단 금지(선택)
    // if (stat.getTotalCnt() != null && stat.getTotalCnt() < 5) return build(stat, KEEP, "표본이 적어 우선 유지합니다.");

    // 1) 비필수인데 대부분 스킵 -> 제거 후보
    if (!required && notRequiredRate >= 0.70) {
      return build(stat, REMOVE_CANDIDATE,
          "비필수 항목이며 대부분 NOT_REQUIRED(스킵) 처리됩니다. 제거/정리 후보입니다.");
    }

    // 2) 비필수인데 대부분 완료 -> 필수 승격 후보
    if (!required && doneRate >= 0.85) {
      return build(stat, INSIGHT_CANDIDATE,
          "비필수 항목이지만 완료 비율이 매우 높습니다. 필수 승격(또는 강조)을 고려하세요.");
    }

    // 3) 필수인데 완료율 낮거나 미완료율 높음 -> 문구/설명 개선
    if (required && (doneRate < 0.60 || notDoneRate >= 0.30)) {
      return build(stat, IMPROVE_COPY,
          "필수 항목이지만 완료/미완료 지표가 좋지 않습니다. 설명 또는 표현 개선이 필요합니다.");
    }

    // 4) 만족도 영향이 큰 항목 -> 인사이트(보조 룰)
    if (delta != null && delta >= 0.5) {
      return build(stat, INSIGHT_CANDIDATE,
          "완료 여부에 따라 만족도 차이가 큰 핵심 항목입니다.");
    }

    // 5) 기본 유지
    return build(stat, KEEP, "현재 상태를 유지해도 무방합니다.");
  }

  private static double nzd(Double v) {
    return v == null ? 0.0 : v;
  }

  private static AiPostItemSignalDTO build(
      AiPostItemStatDTO stat,
      PostChecklistSignalType type,
      String reason
  ) {
    return AiPostItemSignalDTO.builder()
        .itemId(stat.getItemId())
        .itemOrder(stat.getItemOrder())
        .title(stat.getTitle())
        .requiredYn(stat.getRequiredYn())
        .signal(type)
        .reason(reason)
        .build();
  }
}
