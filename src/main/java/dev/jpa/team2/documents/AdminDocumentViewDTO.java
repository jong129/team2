package dev.jpa.team2.documents;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDocumentViewDTO {

  // documents
  private Long docId;
  private Long userId;
  private String docType;
  private String filePath;
  private String imageUrl;   // ✅ 추가
  private String status;
  private Date createdAt;

  // report (없을 수도 있음)
  private Long reportId;
  private String policyVersion;
  private Integer riskScore;
  private String reasonsJson;
  private String parsedJson;
  private String aiExplanation;
  private Date reportCreatedAt;

  public static AdminDocumentViewDTO from(Documents d, DocumentReport r) {
    AdminDocumentViewDTO dto = new AdminDocumentViewDTO();

    dto.setDocId(d.getDocId());
    dto.setUserId(d.getUserId());
    dto.setDocType(d.getDocType());
    dto.setFilePath(d.getFilePath());

    // ✅ 핵심: 로컬 경로 → 브라우저용 URL
    dto.setImageUrl(toImageUrl(d.getFilePath()));

    dto.setStatus(d.getStatus());
    dto.setCreatedAt(d.getCreatedAt());

    if (r != null) {
      dto.setReportId(r.getReportId());
      dto.setPolicyVersion(r.getPolicyVersion());
      dto.setRiskScore(r.getRiskScore());
      dto.setReasonsJson(r.getReasonsJson());
      dto.setParsedJson(r.getParsedJson());
      dto.setAiExplanation(r.getAiExplanation());
      dto.setReportCreatedAt(r.getCreatedAt());
    }

    return dto;
  }

  // ✅ static 이어야 함
  private static String toImageUrl(String filePath) {
    if (filePath == null || filePath.isBlank()) return null;

    // C:\kd\team2\team2\documents\storage\제너스빌_303호_1_20.jpg
    String filename = Paths.get(filePath).getFileName().toString();

    // 한글/공백 URL 인코딩
    String encoded = URLEncoder
        .encode(filename, StandardCharsets.UTF_8)
        .replace("+", "%20");

    return "/files/" + encoded;
  }
}
