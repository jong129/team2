package dev.jpa.team2.checklist.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ============================================
 * POST 체크리스트 분기 판단 로그 엔티티
 * - PRE → POST 분기 의사결정 기록
 * ============================================
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "POST_DECISION_LOG")
@SequenceGenerator(
    name = "SEQ_POST_DECISION_LOG_GEN",
    sequenceName = "SEQ_POST_DECISION_LOG_ID",
    allocationSize = 1
)
public class PostDecisionLog {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "SEQ_POST_DECISION_LOG_GEN"
    )
    @Column(name = "DECISION_ID")
    private Long decisionId;

    /**
     * 기준 PRE 세션 ID
     * (FK 미사용 – 논리적 참조)
     */
    @Column(name = "PRE_SESSION_ID", nullable = false)
    private Long preSessionId;

    /**
     * 생성된 POST 세션 ID
     * (생성 실패/지연 대비 nullable)
     */
    @Column(name = "POST_SESSION_ID")
    private Long postSessionId;

    /**
     * 분기 결과
     * POST_A / POST_B
     */
    @Column(name = "RESULT_CODE", length = 20, nullable = false)
    private String resultCode;

    /**
     * 중요도 기준 초과 항목 ID 목록 (CSV)
     * 예: "12,15,22"
     */
    @Column(name = "HIGH_RISK_ITEM_IDS", length = 1000)
    private String highRiskItemIds;

    /**
     * NOT_DONE 항목 누적 위험 점수
     */
    @Column(name = "RISK_SCORE_SUM", precision = 5, scale = 2)
    private BigDecimal riskScoreSum;

    /**
     * 사람이 읽는 분기 사유 요약
     */
    @Column(name = "DECISION_REASON", length = 1000)
    private String decisionReason;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Date createdAt = new Date();
}
