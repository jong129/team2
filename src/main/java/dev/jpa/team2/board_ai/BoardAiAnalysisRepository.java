package dev.jpa.team2.board_ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardAiAnalysisRepository extends JpaRepository<BoardAiAnalysis, Long> {
    Optional<BoardAiAnalysis> findTopByBoardIdAndAiTypeOrderByCreatedAtDesc(Long boardId, String aiType);
}
