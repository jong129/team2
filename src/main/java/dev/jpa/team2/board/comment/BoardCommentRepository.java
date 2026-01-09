package dev.jpa.team2.board.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

  List<BoardComment> findByBoardIdOrderByCreatedAtAscCommentIdAsc(Long boardId);

  // 게시글 삭제 시 전체 댓글 삭제 용도
  void deleteByBoardId(Long boardId);

  // 부모 댓글 삭제 시 대댓글 먼저 삭제(2-depth 기준)
  void deleteByParentId(Long parentId);

  long countByParentId(Long parentId);
}
