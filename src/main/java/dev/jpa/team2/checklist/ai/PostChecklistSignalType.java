package dev.jpa.team2.checklist.ai;

public enum PostChecklistSignalType {
  KEEP,                 // 유지
  IMPROVE_COPY,         // 문구/설명 개선
  IMPROVE_TIMING,       // 순서/타이밍 개선
  REMOVE_CANDIDATE,     // 제거/교체 후보
  INSIGHT_CANDIDATE     // 만족도 영향 큰 핵심 항목
}
