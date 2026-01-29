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
@AllArgsConstructor         // 전체 필드 생성자
public class DocumentsDTO {
  private Long docId;

  private Long userId;

  private String docType;     // CONTRACT(계약서) / REGISTRY(등본) / BUILDING(건축물대장)

  private List<String> filePaths;

  private String status = "UPLOADED";   // 업로드/분석중/완료

  private Date createdAt = new Date();
  
  // 파일 업로드 관련
  // -----------------------------------------------------------------------------------
  /**
  이미지 파일
  <input type='file' class="form-control" name='file1MF' id='file1MF' 
             value='' placeholder="파일 선택">
  */
  private List<MultipartFile> fileMFs = null;

//-----------------------------------------------------------------------------------

  public DocumentsDTO(Long docId, Long userId, String docType,
                      List<String> filePaths, String status, Date createdAt) {
    this.docId = docId;
    this.userId = userId;
    this.docType = docType;
    this.filePaths = filePaths;
    this.status = status;
    this.createdAt = createdAt;
  }
  public Documents toEntity() {
    return new Documents(userId, docType, filePaths, status, createdAt);
  }

}
