package dev.jpa.team2.board.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardFileRepository extends JpaRepository<BoardFile, Long> {
  List<BoardFile> findByBoardIdOrderByFileIdAsc(Long boardId);
  void deleteByBoardId(Long boardId); // 글 하드삭제 전에 정리용
}
