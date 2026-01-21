package dev.jpa.team2.board_ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiPromptRepository extends JpaRepository<AiPrompt, String> {
    Optional<AiPrompt> findTopByAiTypeAndUseYnOrderByCreatedAtDesc(String aiType, String useYn);
}
