package dev.jpa.team2.checklist.template.repository;

import dev.jpa.team2.checklist.template.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistItemRepository
        extends JpaRepository<ChecklistItem, Long> {
    // 기본 CRUD만 사용
}
