package dev.jpa.team2.documents;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "REGISTRY")
public class Registry {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "REGISTRY_ID")
    private Long registryId;
	
    @Column(name = "ANALYSIS_ID", nullable = false)
    private Long analysisId;
    
    @Column(name = "IMAGE_PATH", nullable = false)
    private String imagePath;
    
    @Column(name = "POLICY_VERSION", nullable = false)
    private String policyVersion;

    @Column(name = "DOC_RISK_SCORE", nullable = false)
    private Long riskScore;
    @Lob
    @Column(name = "AI_EXPLANATION", nullable = false)
    private String aiExplanation;
  
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public Registry() {
		   
	  }
    public Registry(long analysisId,String imagePath,String policyVersion, long riskScore, String aiExplanation) {
    	this.analysisId=analysisId;
    	this.imagePath=imagePath;
    	this.policyVersion=policyVersion;
    	this.riskScore=riskScore;
    	this.aiExplanation=aiExplanation;
    }
}
