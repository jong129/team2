package dev.jpa.team2.documents;

import java.time.LocalDateTime;

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
public class ContractDTO {
    private Long contractId;

    private Long analysisId;

    private String imagePath;

    private String policyVersion;

    private Long riskScore;

    private String aiExplanation;

    private LocalDateTime createdAt;
    
    public ContractDTO(long analysisId,String imagePath,String policyVersion, long riskScore, String aiExplanation) {
    	this.analysisId=analysisId;
    	this.imagePath=imagePath;
    	this.policyVersion=policyVersion;
    	this.riskScore=riskScore;
    	this.aiExplanation=aiExplanation;
    }
    
    public Contract toEntity() {
    	return new Contract(analysisId, imagePath, policyVersion, riskScore, aiExplanation);
    }
}
