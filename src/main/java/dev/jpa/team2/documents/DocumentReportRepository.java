package dev.jpa.team2.documents;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentReportRepository extends JpaRepository<DocumentReport, Long> {

  List<DocumentReport> findByDocIdIn(List<Long> docIds);

  // ✅ docId 컬럼으로 조회
  Optional<DocumentReport> findByDocId(Long docId);

  // ✅ 존재 여부 체크 (서비스에서 쓰고 싶으면)
  boolean existsByDocId(Long docId);

  // ✅ docId 컬럼 기준 삭제
  void deleteByDocId(Long docId);
}
