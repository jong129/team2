package dev.jpa.team2.documents;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report,Long> {
    Report findByAnalysisId(Long analysisId);
    void deleteByAnalysisId(Long analysisId);
    Optional<Report> findOptionalByAnalysisId(Long analysisId);
}
