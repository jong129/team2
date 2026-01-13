package dev.jpa.team2.documents;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "DOCUMENT_REPORT") // ✅ 여기 테이블명 그대로 들어감
public class DocumentReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // 오라클이 IDENTITY 지원이면 OK
  @Column(name = "REPORT_ID")
  private Long reportId;

  @Column(name = "DOC_ID", nullable = false)
  private Long docId;

  @Column(name = "DOC_TYPE", length = 20)
  private String docType; // CONTRACT/REGISTRY/BUILDING/UNKNOWN (선택)

  @Column(name = "POLICY_VERSION", length = 30, nullable = false)
  private String policyVersion;

  @Column(name = "RISK_SCORE", nullable = false)
  private Integer riskScore;

  // JSON 문자열 저장 (CLOB)
  @Column(name = "REASONS_JSON", columnDefinition = "CLOB")
  private String reasonsJson;

  @Column(name = "AI_EXPLANATION", columnDefinition = "CLOB")
  private String aiExplanation;

  @Column(name = "PARSED_JSON", columnDefinition = "CLOB", nullable = false)
  private String parsedJson;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_AT")
  private Date createdAt = new Date();

  // ---------------------------------
  // 기본 생성자
  // ---------------------------------
  public DocumentReport() {
  }

  // ---------------------------------
  // 편의 생성자(필요한 만큼만)
  // ---------------------------------
  public DocumentReport(Long docId, String docType, String policyVersion, Integer riskScore, String reasonsJson,
      String aiExplanation, String parsedJson) {
    this.docId = docId;
    this.docType = docType;
    this.policyVersion = policyVersion;
    this.riskScore = riskScore;
    this.reasonsJson = reasonsJson;
    this.aiExplanation = aiExplanation;
    this.parsedJson = parsedJson;
    this.createdAt = new Date();
  }
}
