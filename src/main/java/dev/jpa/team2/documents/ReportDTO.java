package dev.jpa.team2.documents;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor          // 기본 생성자 자동 생성
@AllArgsConstructor 
public class ReportDTO {
	    private Long reportId;

	    private Long analysisId;

	    private Long totalRiskScore;

	    private String summary;

	    private LocalDateTime createdAt;
	    
	    public ReportDTO(long analysisId,long totalRiskScore,String summary) {
	    	this.analysisId=analysisId;
	    	this.totalRiskScore=totalRiskScore;
	    	this.summary=summary;
	    }
	    
	    public Report toEntity() {
	    	return new Report(analysisId, totalRiskScore, summary);
	    }
}
