package dev.jpa.team2.documents;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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

  private String filePath;

  private String status = "UPLOADED";   // 업로드/분석중/완료

  private Date createdAt = new Date();
  
  // 파일 업로드 관련
  // -----------------------------------------------------------------------------------
  /**
  이미지 파일
  <input type='file' class="form-control" name='file1MF' id='file1MF' 
             value='' placeholder="파일 선택">
  */
  private MultipartFile file1MF = null;
  /** 메인 이미지 크기 단위, 파일 크기 */
  private String size1_label = "";
  /** 메인 이미지 */
  private String file1 = "";
  /** 실제 저장된 메인 이미지 */
  private String file1saved = "";
  /** 메인 이미지 preview */
  private String thumb1 = "";
  /** 메인 이미지 크기 */
  private long size1 = 0;
//-----------------------------------------------------------------------------------

  public DocumentsDTO(Long docId, Long userId, String docType,
                      String filePath, String status, Date createdAt) {
    this.docId = docId;
    this.userId = userId;
    this.docType = docType;
    this.filePath = filePath;
    this.status = status;
    this.createdAt = createdAt;
  }
  public Documents toEntityWithFile() {
    return new Documents(userId, docType, filePath, status, createdAt, file1, file1saved, thumb1, size1);
  }
}
