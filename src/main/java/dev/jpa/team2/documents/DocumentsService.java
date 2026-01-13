package dev.jpa.team2.documents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class DocumentsService {
  
  @Autowired
  DocumentsRepository documentsRepository;
  @Autowired
  DocumentReportRepository documentReportRepository;
  /**
   * 파일 수정
   * 
   * @param file1
   * @param file1saved
   * @param thumb1
   * @param size1
   * @param contentsno
   * @return
   */
  public int update_file1(String file1, String file1saved, String thumb1, long size1, long contentsno) {
    return this.documentsRepository.update_file1(file1, file1saved, thumb1, size1, contentsno);
  }
  
  @Transactional
  public Documents save(DocumentsDTO dto) {
    // ✅ DTO -> Entity 변환 (프로젝트에 맞게 필드 매핑)
    Documents doc = dto.toEntity();
    // doc.set... 필요한 필드 계속 매핑

    Documents saved = documentsRepository.save(doc);

    // (선택) DTO에도 docId 채워두면 이후 로직에서 편함
    dto.setDocId(saved.getDocId());

    return saved;
  }
  @Transactional(readOnly = true)
  public List<AdminDocumentViewDTO> showByUserId(Long userId) {
    List<Documents> docs = documentsRepository.findByUserIdOrderByDocIdDesc(userId);
    if (docs.isEmpty()) return List.of();

    List<Long> docIds = docs.stream()
        .map(Documents::getDocId)
        .filter(Objects::nonNull)
        .toList();

    Map<Long, DocumentReport> reportMap = new HashMap<>();
    if (!docIds.isEmpty()) {
      List<DocumentReport> reports = documentReportRepository.findByDocIdIn(docIds);
      for (DocumentReport r : reports) {
        if (r != null && r.getDocId() != null) {
          reportMap.put(r.getDocId(), r);
        }
      }
    }

    List<AdminDocumentViewDTO> result = new ArrayList<>();
    for (Documents d : docs) {
      result.add(AdminDocumentViewDTO.from(d, reportMap.get(d.getDocId())));
    }
    return result;
  }
  @Transactional
  public void deleteReportByDocId(Long docId) {

    // 1) 문서 존재 확인
    Documents doc = documentsRepository.findById(docId)
        .orElseThrow(() -> new IllegalArgumentException("문서가 존재하지 않습니다. docId=" + docId));

    // 2) 레포트 존재 확인
    boolean hasReport = documentReportRepository.existsByDocId(docId);
    if (!hasReport) {
      throw new IllegalArgumentException("레포트가 존재하지 않습니다. docId=" + docId);
    }

    // 3) 레포트 삭제
    documentReportRepository.deleteByDocId(docId);
    // 4) 문서 상태 업데이트 (원하는 값으로)
    doc.setStatus("REPORT_DELETED");
    documentsRepository.save(doc);
  }
  
  @Transactional
  public void deleteDocumentByDocId(Long docId) {
    // 1) 문서 존재 확인
    Documents doc = documentsRepository.findById(docId)
        .orElseThrow(() -> new IllegalArgumentException("문서가 존재하지 않습니다. docId=" + docId));
    boolean hasReport = documentReportRepository.existsByDocId(docId);
    if(hasReport) {
      documentReportRepository.deleteByDocId(docId);      
    }
    documentsRepository.deleteByDocId(docId);
  }

}
