package dev.jpa.team2.documents;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDocumentWithReportDTO {
  // documents
  private Long docId;
  private Long userId;
  private String docType;
  private String filePath;
  private String status;
  private Date createdAt;

  // report (없을 수 있음)
  private String policyVersion;
  private Integer riskScore;
  private String reasonsJson;
  private String parsedJson;
  private String aiExplanation;
  private Boolean hasReport; // 프론트에서 카드 표시용

  public static UserDocumentWithReportDTO from(Documents d, DocumentReport r) {
    UserDocumentWithReportDTO dto = new UserDocumentWithReportDTO();
    dto.setDocId(d.getDocId());
    dto.setUserId(d.getUserId());
    dto.setDocType(d.getDocType());
    dto.setFilePath(d.getFilePath());
    dto.setStatus(d.getStatus());
    dto.setCreatedAt(d.getCreatedAt());

    if (r != null) {
      dto.setPolicyVersion(r.getPolicyVersion());
      dto.setRiskScore(r.getRiskScore());
      dto.setReasonsJson(r.getReasonsJson());
      dto.setParsedJson(r.getParsedJson());
      dto.setAiExplanation(r.getAiExplanation());
      dto.setHasReport(true);
    } else {
      dto.setHasReport(false);
    }
    return dto;
  }
}
