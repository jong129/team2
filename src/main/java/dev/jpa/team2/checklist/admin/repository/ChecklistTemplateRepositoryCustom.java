package dev.jpa.team2.checklist.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.jpa.team2.checklist.admin.dto.AdminChecklistTemplateRowDto;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.TemplateStatus;

/**
 * 관리자 체크리스트 템플릿 조회용 Custom Repository
 */
public interface ChecklistTemplateRepositoryCustom {

    /**
     * 관리자 템플릿 목록 조회
     */
  Page<AdminChecklistTemplateRowDto> findAdminTemplateList(
      ChecklistPhase phase,
      TemplateStatus status,
      String keyword,
      Pageable pageable
      );
}
