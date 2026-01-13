package dev.jpa.team2.board.like;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {

  boolean existsByBoardIdAndMemberId(Long boardId, Long memberId);

  void deleteByBoardIdAndMemberId(Long boardId, Long memberId);

  long countByBoardId(Long boardId);

  void deleteByBoardId(Long boardId); // 게시글 하드삭제 시 정리용(선택)
}
