package dev.jpa.team2.checklist.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.model.ChecklistItemMaster;

public interface ItemMasterRepository
        extends JpaRepository<ChecklistItemMaster, Long> {
}
