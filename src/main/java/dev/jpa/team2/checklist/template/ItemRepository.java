package dev.jpa.team2.checklist.template;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository
        extends JpaRepository<Item, Long> {
    // 기본 CRUD만 사용
}
