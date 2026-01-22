package dev.jpa.team2.checklist.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.model.ChecklistItemMaster;

public interface ChecklistItemMasterRepository
    extends JpaRepository<ChecklistItemMaster, Long>, ChecklistItemMasterRepositoryCustom {

  /**
   * 🔥 중복이 있어도 1건만 조회 (최신 것 우선)
   */
  Optional<ChecklistItemMaster> findFirstByPhaseAndPostGroupCodeAndTitleOrderByItemMasterIdDesc(String phase,
      String postGroupCode, String title);
}
