package dev.jpa.team2.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContractService {
	@Autowired
	ContractRepository contractRepository;
	public Contract save(ContractDTO dto) {
	    // ✅ DTO -> Entity 변환 (프로젝트에 맞게 필드 매핑)
		Contract contract = dto.toEntity();
		Contract saved = contractRepository.save(contract);

	    // (선택) DTO에도 docId 채워두면 이후 로직에서 편함
		dto.setContractId(saved.getContractId());

	    return saved;
	  }
}
