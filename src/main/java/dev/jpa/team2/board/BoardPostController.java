package dev.jpa.team2.board;

import dev.jpa.team2.tool.PageResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board/posts")
public class BoardPostController {

  private final BoardPostService boardPostService;

  // 목록
  @GetMapping
  public ResponseEntity<PageResponse<BoardPostDto>> list(
      @RequestParam("categoryId") Long categoryId,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(boardPostService.list(categoryId, keyword, pageable));
  }

  // 상세 (비밀글/권한이 있으면 memberId 전달)
  @GetMapping("/{boardId}")
  public ResponseEntity<BoardPostDto> read(
      @PathVariable("boardId") Long boardId,
      HttpSession session
  ) {
    Long loginMemberId = getLoginMemberId(session); // 없으면 null (공개글이면 읽기 가능)
    return ResponseEntity.ok(boardPostService.read(boardId, loginMemberId));
  }

  // 작성 (로그인 필수)
  @PostMapping
  public ResponseEntity<BoardPostDto> create(
      @RequestBody BoardPostCreateRequest req,
      HttpSession session
  ) {
    Long memberId = requireLogin(session);

    // ✅ 권장: 세션에서 꺼내지 말고 service에서 memberId로 조회해서 채우는 구조로 가는 게 안정적
    // 그래서 loginId/name은 null로 보내고 서비스에서 member 조회로 채우는 방식 추천
    return ResponseEntity.ok(boardPostService.create(memberId, null, null, req));
  }

  // 수정 (로그인 필수)
  @PutMapping("/{boardId}")
  public ResponseEntity<BoardPostDto> update(
      @PathVariable("boardId") Long boardId,
      @RequestBody BoardPostUpdateRequest req,
      HttpSession session
  ) {
    Long memberId = requireLogin(session);
    return ResponseEntity.ok(boardPostService.update(boardId, memberId, req));
  }

  // 삭제(soft delete) (로그인 필수)
  @DeleteMapping("/{boardId}")
  public ResponseEntity<Void> delete(
      @PathVariable("boardId") Long boardId,
      HttpSession session
  ) {
    Long memberId = requireLogin(session);
    boardPostService.delete(boardId, memberId);
    return ResponseEntity.ok().build();
  }

  private Long requireLogin(HttpSession session) {
    Long memberId = getLoginMemberId(session);
    if (memberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
    return memberId;
  }

  private Long getLoginMemberId(HttpSession session) {
    Object v = session.getAttribute("LOGIN_MEMBER_ID");
    if (v instanceof Long l) return l;
    if (v instanceof Integer i) return i.longValue();
    if (v instanceof String s) {
      try { return Long.parseLong(s); } catch (Exception ignored) {}
    }
    return null;
  }
}


