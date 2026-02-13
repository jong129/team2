package dev.jpa.team2.documents;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ANALYSIS")
@Inheritance(strategy = InheritanceType.JOINED)
public class Analysis {
	@Id
	  @GeneratedValue(strategy = GenerationType.IDENTITY)   // 기본키
	  @Column(name = "ANALYSIS_ID")
	  private Long analysisId;

	  @Column(name = "USER_ID", nullable = false)
	  private Long userId;

	  @Column(name = "DOC_COUNT", length = 50, nullable = false)
	  private Long docCount;     // CONTRACT(계약서) / REGISTRY(등본) / BUILDING(건축물대장)

	  @Temporal(TemporalType.TIMESTAMP)
	  @Column(name = "CREATED_AT")
	  private Date createdAt = new Date();
	  
	  @Transient
	  private List<String> filePaths;
	  
	  public Analysis() {
		   
	  }
	  
	  public Analysis(long userId, Long docCount, Date createdAt) {
		  this.userId=userId;
		  this.docCount=docCount;
		  this.createdAt=createdAt;
	  }
	  public Analysis(long userId, Long docCount, Date createdAt, List<String> filePaths) {
		  this.userId=userId;
		  this.docCount=docCount;
		  this.createdAt=createdAt;
		  this.filePaths=filePaths;
	  }
}
