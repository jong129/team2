package dev.jpa.team2.documents;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 전체 필드 생성자
public class DocumentReportDTO {

  private Long reportId; // DOCUMENT_REPORT.REPORT_ID

  private Long docId; // DOCUMENTS.DOC_ID (FK)

  private String docType; // CONTRACT / REGISTRY / BUILDING / UNKNOWN

  private String policyVersion;

  private Integer riskScore;

  /** 판단 사유 리스트 (JSON 문자열: ["사유1","사유2"]) */
  private String reasonsJson;

  /** AI 설명 (긴 텍스트) */
  private String aiExplanation;

  /** Vision 파싱 결과 전체 JSON */
  private String parsedJson;

  private Date createdAt = new Date();

  // -------------------------------------------------
  // 필요한 최소 생성자 (주로 이거 씀)
  // -------------------------------------------------
  public DocumentReportDTO(
      Long docId,
      String docType,
      String policyVersion,
      Integer riskScore,
      String reasonsJson,
      String aiExplanation,
      String parsedJson
  ) {
    this.docId = docId;
    this.docType = docType;
    this.policyVersion = policyVersion;
    this.riskScore = riskScore;
    this.reasonsJson = reasonsJson;
    this.aiExplanation = aiExplanation;
    this.parsedJson = parsedJson;
    this.createdAt = new Date();
  }
  
  // -------------------------------------------------
  // DTO → Entity 변환
  // -------------------------------------------------
  public DocumentReport toEntity() {
    return new DocumentReport(docId,  docType, policyVersion,  riskScore,  reasonsJson,
        aiExplanation, parsedJson);
  }
}
