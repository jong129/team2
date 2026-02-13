package dev.jpa.team2.documents;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract,Long>{
	List<Contract> findAllByAnalysisId(Long analysisId);
	void deleteAllByAnalysisId(Long analysisId);
}
