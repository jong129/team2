package dev.jpa.team2.checklist.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.model.ChecklistItemMaster;

public interface ChecklistItemMasterRepository
        extends JpaRepository<ChecklistItemMaster, Long>,
                ChecklistItemMasterRepositoryCustom {
}
