package dev.jpa.team2.board_ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardAiDraftRepository extends JpaRepository<BoardAiDraft, Long> {

    // ServiceImpl에서 쓰는 메서드 시그니처 그대로
    Optional<BoardAiDraft> findTopByCategoryIdAndInputHashOrderByCreatedAtDesc(Long categoryId, String inputHash);
}
