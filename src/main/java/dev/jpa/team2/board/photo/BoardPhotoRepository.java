package dev.jpa.team2.board.photo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardPhotoRepository extends JpaRepository<BoardPhoto, Long> {
  List<BoardPhoto> findByBoardIdOrderByPhotoIdAsc(Long boardId);
}
