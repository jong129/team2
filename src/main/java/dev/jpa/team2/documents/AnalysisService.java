package dev.jpa.team2.documents;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AnalysisService {
	@Autowired
	AnalysisRepository analysisRepository;
	@Autowired
	ReportRepository reportRepository;
	@Autowired
	ContractRepository contractRepository;
	@Autowired
	RegistryRepository registryRepository;
	@Transactional
	public Analysis save(AnalysisDTO dto) {
	    // ✅ DTO -> Entity 변환 (프로젝트에 맞게 필드 매핑)
		Analysis analysis = dto.toEntity();
		Analysis saved = analysisRepository.save(analysis);

	    // (선택) DTO에도 docId 채워두면 이후 로직에서 편함
		dto.setAnalysisId(saved.getAnalysisId());

	    return saved;
	  }
	public List<AdminAnalysisViewDTO> showByUserId(long userId)
	{
		List<AdminAnalysisViewDTO> loaded=new ArrayList<>();
		List<Analysis> analysisList = analysisRepository.findByUserIdOrderByCreatedAtDesc(userId);
		for (Analysis item : analysisList) {
			Report loadedReport=reportRepository.findByAnalysisId(item.getAnalysisId());
			List<Contract> loadedContract=contractRepository.findAllByAnalysisId(item.getAnalysisId());
			List<Registry> loadedRegistry=registryRepository.findAllByAnalysisId(item.getAnalysisId());
			AdminAnalysisViewDTO loadedItem=new AdminAnalysisViewDTO(userId,item,loadedReport,loadedContract,loadedRegistry);
			loaded.add(loadedItem);
		}
		final ObjectMapper om = new ObjectMapper()
		        .registerModule(new JavaTimeModule())
		        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		        .enable(SerializationFeature.INDENT_OUTPUT);
		try {
	        log.info("[SHOW RESULT userId={}] \n{}",
	                userId,
	                om.writeValueAsString(loaded)
	        );
	    } catch (Exception e) {
	        log.warn("❌ showByUserId 로그 직렬화 실패", e);
	    }
		return loaded;
	}
	public void deleteAllByAnalysisId(long analysisId) {
		analysisRepository.deleteByAnalysisId(analysisId);
		reportRepository.deleteByAnalysisId(analysisId);
		contractRepository.deleteAllByAnalysisId(analysisId);
		registryRepository.deleteAllByAnalysisId(analysisId);
	}
}
