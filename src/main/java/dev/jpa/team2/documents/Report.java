package dev.jpa.team2.documents;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "REPORT")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REPORT_ID")
    private Long reportId;

    // FK 컬럼만 들고 가는 방식(간단)
    @Column(name = "ANALYSIS_ID", nullable = false)
    private Long analysisId;

    @Column(name = "TOTAL_RISK_SCORE", nullable = false)
    private Long totalRiskScore;

    @Lob
    @Column(name = "SUMMARY")
    private String summary;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public Report() {
		   
	  }
    
    public Report(long analysisId,long totalRiskScore,String summary) {
    	this.analysisId=analysisId;
    	this.totalRiskScore=totalRiskScore;
    	this.summary=summary;
    }
}
