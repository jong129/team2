package dev.jpa.team2.documents;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistryRepository extends JpaRepository<Registry, Long> {
	List<Registry> findAllByAnalysisId(Long analysisId);
	void deleteAllByAnalysisId(Long analysisId);
}
