package dev.jpa.team2.board.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {

  boolean existsByCategoryName(String categoryName);

  List<BoardCategory> findAllByOrderBySortNoAscCategoryIdAsc();

  List<BoardCategory> findAllByVisibleYnOrderBySortNoAscCategoryIdAsc(String visibleYn);
}
