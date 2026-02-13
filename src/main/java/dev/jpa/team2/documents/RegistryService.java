package dev.jpa.team2.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistryService {
	@Autowired
	RegistryRepository registryRepository;
	public Registry save(RegistryDTO dto) {
	    // ✅ DTO -> Entity 변환 (프로젝트에 맞게 필드 매핑)
		Registry registry = dto.toEntity();
		Registry saved = registryRepository.save(registry);

	    // (선택) DTO에도 docId 채워두면 이후 로직에서 편함
		dto.setRegistryId(saved.getRegistryId());

	    return saved;
	  }
}
