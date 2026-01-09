package dev.jpa.team2.board.report;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardReportRepository extends JpaRepository<BoardReport, Long> {
  boolean existsByBoardIdAndMemberId(Long boardId, Long memberId);
  long countByBoardId(Long boardId);
  void deleteByBoardId(Long boardId); // 게시글 하드삭제 시 정리(선택)
}
