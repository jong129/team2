package dev.jpa.team2.checklist.policy;

/**
 * 사전 체크리스트 위험 판단 정책 상수
 *
 * ❗ 이 클래스는 "정책 정의" 전용이다.
 * ❗ 비즈니스 로직은 절대 넣지 않는다.
 *
 * [사용 목적]
 * - POST_A / POST_B 분기 기준을 코드로 명확히 고정
 * - 매직 넘버(70, 80) 제거
 * - 정책 변경 시 한 곳만 수정하도록 하기 위함
 */
public final class PreRiskPolicy {

    /**
     * 누적 위험 점수 기준
     *
     * - AI가 계산한 전체 위험 점수(riskScoreSum)
     * - 이 값 이상이면 고위험(POST_B) 후보
     */
    public static final double TOTAL_RISK_THRESHOLD = 70.0;

    /**
     * 단일 항목 최고 위험 점수 기준
     *
     * - 개별 체크리스트 항목 importanceScore
     * - 하나라도 이 값 이상이면 즉시 고위험(POST_B)
     */
    public static final double SINGLE_ITEM_THRESHOLD = 80.0;

    /**
     * 인스턴스 생성 방지
     */
    private PreRiskPolicy() {}
}
