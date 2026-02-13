package dev.jpa.team2.documents;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis,Long>{
	void deleteByAnalysisId(long analysisId);
	List<Analysis> findByUserIdOrderByCreatedAtDesc(Long userId);
}
