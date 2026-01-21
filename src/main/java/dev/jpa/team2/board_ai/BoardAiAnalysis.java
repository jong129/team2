package dev.jpa.team2.board_ai;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BOARD_AI_ANALYSIS")
@Getter
@Setter
public class BoardAiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BOARD_AI_ANALYSIS_GEN")
    @SequenceGenerator(
            name = "SEQ_BOARD_AI_ANALYSIS_GEN",
            sequenceName = "SEQ_BOARD_AI_ANALYSIS_ID",
            allocationSize = 1
    )
    @Column(name = "AI_ANALYSIS_ID")
    private Long aiAnalysisId;

    @Column(name = "BOARD_ID", nullable = false)
    private Long boardId;

    @Column(name = "CATEGORY_ID", nullable = false)
    private Long categoryId;

    @Column(name = "AI_TYPE", nullable = false, length = 30)
    private String aiType; // SUMMARY / SENTIMENT

    @Lob
    @Column(name = "AI_RESULT", nullable = false)
    private String aiResult;

    @Column(name = "AI_SCORE", precision = 3, scale = 2)
    private BigDecimal aiScore;

    @Column(name = "PROMPT_CODE", nullable = false, length = 50)
    private String promptCode;

    @Column(name = "MODEL_NAME", nullable = false, length = 50)
    private String modelName;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
