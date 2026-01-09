package dev.jpa.team2.board;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

  @Query("""
        select p
        from BoardPost p
        where p.deletedYn = 'N'
          and p.category.categoryId = :categoryId
          and (
            :keyword is null
            or lower(p.title) like lower(concat('%', :keyword, '%'))
            or lower(p.loginId) like lower(concat('%', :keyword, '%'))
            or lower(p.writerName) like lower(concat('%', :keyword, '%'))
          )
      """)
  Page<BoardPost> findByCategoryIdWithKeyword(
      @Param("categoryId") Long categoryId,
      @Param("keyword") String keyword,
      Pageable pageable
  );

  Optional<BoardPost> findByBoardIdAndDeletedYn(Long boardId, String deletedYn);

  @Transactional
  @Modifying
  @Query("update BoardPost p set p.viewCnt = p.viewCnt + 1 where p.boardId = :boardId and p.deletedYn = 'N'")
  int increaseViewCnt(@Param("boardId") Long boardId);

  // ✅ DB에서 행 자체 삭제 (Hard Delete)
  @Transactional
  @Modifying
  @Query("delete from BoardPost p where p.boardId = :boardId")
  int hardDelete(@Param("boardId") Long boardId);
}