package dev.jpa.team2.checklist.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.jpa.team2.checklist.admin.dto.AdminChecklistItemMasterRowDto;
import dev.jpa.team2.checklist.enums.ChecklistPhase;

/**
 * 관리자 아이템 마스터 조회용 Custom Repository
 */
public interface ChecklistItemMasterRepositoryCustom {

    Page<AdminChecklistItemMasterRowDto> findAdminItemMasters(
            ChecklistPhase phase,
            String postGroupCode,
            String keyword,
            String activeYn,
            Pageable pageable
    );
}
