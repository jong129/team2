package dev.jpa.team2.board;

import dev.jpa.team2.board.category.BoardCategory;
import dev.jpa.team2.board.category.BoardCategoryRepository;
import dev.jpa.team2.board.category.BoardCategoryWritePolicy;
import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;
import dev.jpa.team2.member.member_role.MemberRoleService;
import dev.jpa.team2.tool.PageResponse;
import dev.jpa.team2.board.like.BoardLikeRepository;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardPostService {

  private final BoardPostRepository boardPostRepository;
  private final BoardCategoryRepository boardCategoryRepository;
  private final MemberRoleService memberRoleService;
  private final BoardLikeRepository boardLikeRepository;

  // ✅ 추가
  private final MemberRepository memberRepository;

  // ✅ 추가: 게시글 하드삭제 시 자식 먼저 삭제용
  private final JdbcTemplate jdbcTemplate;

  @Transactional(readOnly = true)
  public PageResponse<BoardPostDto> list(Long categoryId, String keyword, Pageable pageable) {
    Page<BoardPostDto> page = boardPostRepository
        .findByCategoryIdWithKeyword(categoryId, normalize(keyword), pageable)
        .map(BoardPostDto::from);

    return PageResponse.from(page);
  }

  public BoardPostDto read(Long boardId, Long loginMemberId) {
    BoardPost post = boardPostRepository.findByBoardIdAndDeletedYn(boardId, "N")
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));

    if ("Y".equals(post.getSecretYn())) {
      if (loginMemberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
      boolean isAdmin = memberRoleService.isAdmin(loginMemberId);
      if (!isAdmin && !loginMemberId.equals(post.getMemberId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "secret post");
      }
    }

    boardPostRepository.increaseViewCnt(boardId);

    BoardPost refreshed = boardPostRepository.findByBoardIdAndDeletedYn(boardId, "N")
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));

    BoardPostDto dto = BoardPostDto.from(refreshed);

    // ✅ 좋아요 기능이 카테고리에서 꺼져있으면, UI도 숨길 거라 값은 안전하게 N/0
    // (원하면 실제 count를 내려도 되지만, 기능 off면 보통 0/N이 깔끔)
    if (!"Y".equalsIgnoreCase(refreshed.getCategory().getLikeYn())) {
      dto.setLikeCnt(0L);
      dto.setLikedYn("N");
      return dto;
    }

    long likeCnt = boardLikeRepository.countByBoardId(boardId);
    String likedYn = "N";
    if (loginMemberId != null) {
      likedYn = boardLikeRepository.existsByBoardIdAndMemberId(boardId, loginMemberId) ? "Y" : "N";
    }

    dto.setLikeCnt(likeCnt);
    dto.setLikedYn(likedYn);

    return dto;
  }

  public BoardPostDto create(Long loginMemberId, String loginId, String writerName, BoardPostCreateRequest req) {
    if (loginMemberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
    if (req.getCategoryId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId required");
    if (req.getTitle() == null || req.getTitle().trim().isEmpty())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title required");
    if (req.getContent() == null || req.getContent().trim().isEmpty())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content required");

    BoardCategory category = boardCategoryRepository.findById(req.getCategoryId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found"));

    enforceWritePolicy(category, loginMemberId);

    // ✅ memberId로 Member 조회해서 loginId/writerName 확정
    Member m = memberRepository.findById(loginMemberId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "member not found"));

    String resolvedLoginId = m.getLoginId();
    String resolvedWriterName = m.getName();

    if (resolvedLoginId == null || resolvedLoginId.trim().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "member loginId missing");
    }
    if (resolvedWriterName == null || resolvedWriterName.trim().isEmpty()) {
      resolvedWriterName = resolvedLoginId;
    }

    BoardPost p = new BoardPost();
    p.setMemberId(loginMemberId);
    p.setCategory(category);

    p.setTitle(req.getTitle().trim());
    p.setContent(req.getContent());

    // ✅ secretYn 통일
    p.setSecretYn(resolveSecret(category, req.getSecretYn()));
    p.setPostPassword(req.getPostPassword());

    if (!"Y".equals(p.getSecretYn())) {
      p.setPostPassword(null);
    }

    p.setViewCnt(0L);
    p.setPinnedYn("N");
    p.setDeletedYn("N");

    p.setLoginId(resolvedLoginId);
    p.setWriterName(resolvedWriterName);

    p.setCreatedAt(LocalDateTime.now());

    BoardPost saved = boardPostRepository.save(p);
    return BoardPostDto.from(saved);
  }

  public BoardPostDto update(Long boardId, Long loginMemberId, BoardPostUpdateRequest req) {
    if (loginMemberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");

    BoardPost post = boardPostRepository.findByBoardIdAndDeletedYn(boardId, "N")
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));

    boolean isAdmin = memberRoleService.isAdmin(loginMemberId);
    boolean isOwner = loginMemberId.equals(post.getMemberId());

    if (!isAdmin && !isOwner) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no permission");

    if (req.getTitle() != null && !req.getTitle().trim().isEmpty()) post.setTitle(req.getTitle().trim());
    if (req.getContent() != null && !req.getContent().trim().isEmpty()) post.setContent(req.getContent().trim());

    post.setSecretYn(resolveSecret(post.getCategory(), req.getSecretYn()));

    if ("Y".equals(post.getSecretYn())) {
      if (req.getPostPassword() != null) post.setPostPassword(req.getPostPassword());
    } else {
      post.setPostPassword(null);
    }

    if (req.getPinnedYn() != null) {
      if (!isAdmin) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin only pinned");
      post.setPinnedYn(normalizeYn(req.getPinnedYn(), "N"));
    }

    post.setUpdatedAt(LocalDateTime.now());
    return BoardPostDto.from(post);
  }

  public void delete(Long boardId, Long loginMemberId) {
    if (loginMemberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");

    BoardPost post = boardPostRepository.findByBoardIdAndDeletedYn(boardId, "N")
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));

    boolean isAdmin = memberRoleService.isAdmin(loginMemberId);
    boolean isOwner = loginMemberId.equals(post.getMemberId());

    // ✅ 삭제 권한: ADMIN_ONLY 게시판 글은 관리자만
    BoardCategoryWritePolicy policy = post.getCategory().getWritePolicy();
    if (policy == BoardCategoryWritePolicy.ADMIN_ONLY) {
      if (!isAdmin) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin only delete");
    } else {
      if (!isAdmin && !isOwner) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no permission");
    }

    // ✅ 하드삭제: 자식 → 부모
    jdbcTemplate.update("DELETE FROM BOARD_COMMENT WHERE BOARD_ID = ?", boardId);
    jdbcTemplate.update("DELETE FROM BOARD_LIKE WHERE BOARD_ID = ?", boardId);
    jdbcTemplate.update("DELETE FROM BOARD_REPORT WHERE BOARD_ID = ?", boardId);
    jdbcTemplate.update("DELETE FROM BOARD_FILE WHERE BOARD_ID = ?", boardId);
    jdbcTemplate.update("DELETE FROM BOARD_PHOTO WHERE BOARD_ID = ?", boardId);

    boardPostRepository.deleteById(boardId);
  }

  private void enforceWritePolicy(BoardCategory category, Long memberId) {
    BoardCategoryWritePolicy policy = category.getWritePolicy();
    if (policy == BoardCategoryWritePolicy.ADMIN_ONLY) {
      boolean isAdmin = memberRoleService.isAdmin(memberId);
      if (!isAdmin) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin only category");
    }
  }

  private String resolveSecret(BoardCategory category, String requestedSecretYn) {
    if (!"Y".equals(category.getSecretYn())) return "N";
    return normalizeYn(requestedSecretYn, "N");
  }

  private String normalizeYn(String v, String def) {
    if (v == null) return def;
    String t = v.trim().toUpperCase();
    if ("Y".equals(t) || "N".equals(t)) return t;
    return def;
  }

  private String normalize(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}
