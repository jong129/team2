package dev.jpa.team2.board.report;

import dev.jpa.team2.board.BoardPost;
import dev.jpa.team2.board.BoardPostRepository;
import dev.jpa.team2.board.BoardReportCreateRequest;
import dev.jpa.team2.board.BoardReportResponse;
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
public class BoardReportService {

  private final BoardReportRepository boardReportRepository;
  private final BoardPostRepository boardPostRepository;

  public BoardReportResponse create(Long boardId, Long loginMemberId, BoardReportCreateRequest req) {
    requireLogin(loginMemberId);

    BoardCategory category = getCategoryByBoardId(boardId);
    requireEnabled(category.getReportYn(), "report");

    String reasonCode = normalize(req.getReasonCode());
    if (reasonCode == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reasonCode required");

    // 중복 신고 방지 (UQ로도 막히지만, 사용자 메시지 깔끔하게)
    if (boardReportRepository.existsByBoardIdAndMemberId(boardId, loginMemberId)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "already reported");
    }

    BoardReport e = new BoardReport();
    e.setBoardId(boardId);
    e.setMemberId(loginMemberId);
    e.setReasonCode(reasonCode);
    e.setReasonText(normalize(req.getReasonText()));

    try {
      BoardReport saved = boardReportRepository.save(e);
      return BoardReportResponse.ok(saved.getReportId());
    } catch (DataIntegrityViolationException dup) {
      // UNIQUE(boardId, memberId) 동시성 충돌 케이스
      throw new ResponseStatusException(HttpStatus.CONFLICT, "already reported");
    }
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

  private String normalize(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}
