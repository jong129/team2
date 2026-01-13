package dev.jpa.team2.board.comment;

import dev.jpa.team2.board.BoardCommentCreateRequest;
import dev.jpa.team2.board.BoardCommentDto;
import dev.jpa.team2.board.BoardCommentUpdateRequest;
import dev.jpa.team2.board.BoardPost;
import dev.jpa.team2.board.BoardPostRepository;
import dev.jpa.team2.board.category.BoardCategory;
import dev.jpa.team2.member.member_role.MemberRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardCommentService {

  private final BoardCommentRepository boardCommentRepository;
  private final BoardPostRepository boardPostRepository;
  private final MemberRoleService memberRoleService;

  @Transactional(readOnly = true)
  public List<BoardCommentDto> list(Long boardId) {
    BoardCategory category = getCategoryByBoardId(boardId);
    requireEnabled(category.getCommentYn(), "comment");

    return boardCommentRepository.findByBoardIdOrderByCreatedAtAscCommentIdAsc(boardId)
        .stream().map(BoardCommentDto::from).toList();
  }

  public BoardCommentDto create(Long boardId, Long loginMemberId, BoardCommentCreateRequest req) {
    requireLogin(loginMemberId);

    BoardCategory category = getCategoryByBoardId(boardId);
    requireEnabled(category.getCommentYn(), "comment");

    if (req.getContent() == null || req.getContent().trim().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content required");
    }

    // ✅ 대댓글 검증(2-depth만 허용)
    Long parentId = req.getParentId();
    if (parentId != null) {
      BoardComment parent = boardCommentRepository.findById(parentId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "parent comment not found"));

      if (!parent.getBoardId().equals(boardId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "parent comment board mismatch");
      }
      if (parent.getParentId() != null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only 2-depth replies allowed");
      }
    }

    BoardComment c = new BoardComment();
    c.setBoardId(boardId);
    c.setMemberId(loginMemberId);
    c.setParentId(parentId);
    c.setContent(req.getContent().trim());
    c.setIsSecret(normalizeYn(req.getIsSecret(), "N"));

    return BoardCommentDto.from(boardCommentRepository.save(c));
  }

  public BoardCommentDto update(Long commentId, Long loginMemberId, BoardCommentUpdateRequest req) {
    requireLogin(loginMemberId);

    BoardComment c = boardCommentRepository.findById(commentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "comment not found"));

    BoardCategory category = getCategoryByBoardId(c.getBoardId());
    requireEnabled(category.getCommentYn(), "comment");

    boolean isAdmin = memberRoleService.isAdmin(loginMemberId);
    boolean isOwner = loginMemberId.equals(c.getMemberId());
    if (!isAdmin && !isOwner) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no permission");

    if (req.getContent() != null && !req.getContent().trim().isEmpty()) {
      c.setContent(req.getContent().trim());
    }
    if (req.getIsSecret() != null) {
      c.setIsSecret(normalizeYn(req.getIsSecret(), c.getIsSecret()));
    }

    return BoardCommentDto.from(c);
  }

  public void delete(Long commentId, Long loginMemberId) {
    requireLogin(loginMemberId);

    BoardComment c = boardCommentRepository.findById(commentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "comment not found"));

    BoardCategory category = getCategoryByBoardId(c.getBoardId());
    requireEnabled(category.getCommentYn(), "comment");

    boolean isAdmin = memberRoleService.isAdmin(loginMemberId);
    boolean isOwner = loginMemberId.equals(c.getMemberId());
    if (!isAdmin && !isOwner) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no permission");

    // ✅ 하드삭제: 부모 댓글이면 대댓글 먼저 삭제해야 FK_PARENT 때문에 안 막힘
    if (boardCommentRepository.countByParentId(c.getCommentId()) > 0) {
      boardCommentRepository.deleteByParentId(c.getCommentId());
    }

    boardCommentRepository.delete(c);
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

  private String normalizeYn(String v, String def) {
    if (v == null) return def;
    String t = v.trim().toUpperCase();
    return ("Y".equals(t) || "N".equals(t)) ? t : def;
  }
}
