package dev.jpa.team2.documents;

import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

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
public class AnalysisDTO {
	  private Long analysisId;

	  private Long userId;

	  private Long docCount;     // CONTRACT(계약서) / REGISTRY(등본) / BUILDING(건축물대장)

	  private Date createdAt = new Date();
	  
	  private List<String> filePaths;
	  
	  private List<MultipartFile> fileMFs = null;

	  public AnalysisDTO(long userId, Long docCount, Date createdAt) {
		  this.userId=userId;
		  this.docCount=docCount;
		  this.createdAt=createdAt;
	  }
	  
	  public AnalysisDTO(long userId, Long docCount, Date createdAt, List<String> filePaths) {
		  this.userId=userId;
		  this.docCount=docCount;
		  this.createdAt=createdAt;
		  this.filePaths=filePaths;
	  }
	  public Analysis toEntity() {
		    return new Analysis(userId, docCount, createdAt);
		  }
}
