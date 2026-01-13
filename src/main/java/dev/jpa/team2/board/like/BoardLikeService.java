package dev.jpa.team2.board.like;

import dev.jpa.team2.board.BoardLikeCountResponse;
import dev.jpa.team2.board.BoardLikeToggleResponse;
import dev.jpa.team2.board.BoardPost;
import dev.jpa.team2.board.BoardPostRepository;
import dev.jpa.team2.board.category.BoardCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardLikeService {

  private final BoardLikeRepository boardLikeRepository;
  private final BoardPostRepository boardPostRepository;

  public BoardLikeToggleResponse toggle(Long boardId, Long loginMemberId) {
    requireLogin(loginMemberId);

    BoardCategory category = getCategoryByBoardId(boardId);
    requireEnabled(category.getLikeYn(), "like");

    boolean exists = boardLikeRepository.existsByBoardIdAndMemberId(boardId, loginMemberId);

    if (exists) {
      boardLikeRepository.deleteByBoardIdAndMemberId(boardId, loginMemberId); // 하드삭제
      long cnt = boardLikeRepository.countByBoardId(boardId);
      return BoardLikeToggleResponse.of(false, cnt);
    }

    try {
      BoardLike e = new BoardLike();
      e.setBoardId(boardId);
      e.setMemberId(loginMemberId);
      boardLikeRepository.save(e);
    } catch (DataIntegrityViolationException dup) {
      // 동시성으로 UNIQUE(boardId, memberId) 걸린 케이스면 "이미 눌림"으로 처리
    }

    long cnt = boardLikeRepository.countByBoardId(boardId);
    return BoardLikeToggleResponse.of(true, cnt);
  }

  @Transactional(readOnly = true)
  public BoardLikeCountResponse count(Long boardId) {
    BoardCategory category = getCategoryByBoardId(boardId);
    requireEnabled(category.getLikeYn(), "like");
    return BoardLikeCountResponse.of(boardLikeRepository.countByBoardId(boardId));
  }

  private BoardCategory getCategoryByBoardId(Long boardId) {
    BoardPost post = boardPostRepository.findByBoardIdAndDeletedYn(boardId, "N")
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));
    return post.getCategory();
  }

  private void requireEnabled(String yn, String featureName) {
    if (!"Y".equalsIgnoreCase(yn)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, featureName + " disabled");
    }
  }

  private void requireLogin(Long memberId) {
    if (memberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
  }
}
