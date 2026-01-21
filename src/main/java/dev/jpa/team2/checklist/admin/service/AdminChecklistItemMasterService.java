package dev.jpa.team2.checklist.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.admin.dto.AdminChecklistItemMasterRowDto;
import dev.jpa.team2.checklist.admin.repository.ChecklistItemMasterRepository;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminChecklistItemMasterService {

    private final ChecklistItemMasterRepository itemMasterRepository;

    @Transactional(readOnly = true)
    public Page<AdminChecklistItemMasterRowDto> getItemMasters(
            int page,
            int size,
            String phase,
            String postGroupCode,
            String keyword,
            String activeYn
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "itemMasterId")
        );

        ChecklistPhase phaseEnum = null;
        if (phase != null && !phase.isBlank()) {
            phaseEnum = ChecklistPhase.valueOf(phase);
        }

        return itemMasterRepository.findAdminItemMasters(
                phaseEnum,
                postGroupCode,
                keyword,
                activeYn,
                pageable
        );
    }
}
