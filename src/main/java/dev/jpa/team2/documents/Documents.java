package dev.jpa.team2.documents;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "DOCUMENTS")
@Inheritance(strategy = InheritanceType.JOINED)
public class Documents {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)   // 기본키
  @Column(name = "DOC_ID")
  private Long docId;

  @Column(name = "USER_ID", nullable = false)
  private Long userId;

  @Column(name = "DOC_TYPE", length = 50, nullable = false)
  private String docType;     // CONTRACT(계약서) / REGISTRY(등본) / BUILDING(건축물대장)

  @Column(name = "FILE_PATH", length = 500)
  private String filePath;

  @Column(name = "STATUS", length = 20)
  private String status = "UPLOADED";   // 업로드/분석중/완료

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_AT")
  private Date createdAt = new Date();
}