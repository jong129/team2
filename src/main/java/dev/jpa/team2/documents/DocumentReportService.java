package dev.jpa.team2.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DocumentReportService {
  @Autowired
  DocumentReportRepository documentsReportRepository;
  
  public DocumentReport save(DocumentReportDTO documentReportDTO) {
    DocumentReport documentReport = this.documentsReportRepository.save(documentReportDTO.toEntity());
    
    return documentReport;
  }
}
