package dev.jpa.team2.checklist.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistSatisfactionRepository extends JpaRepository<ChecklistSatisfaction, Long> {
  Optional<ChecklistSatisfaction> findBySession_SessionId(Long sessionId);
}
