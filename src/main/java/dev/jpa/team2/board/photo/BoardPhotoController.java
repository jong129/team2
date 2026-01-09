package dev.jpa.team2.board.photo;

import dev.jpa.team2.board.BoardPhotoDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardPhotoController {

  private final BoardPhotoService boardPhotoService;

  @GetMapping("/posts/{boardId}/photos")
  public ResponseEntity<List<BoardPhotoDto>> list(@PathVariable("boardId") Long boardId) {
    return ResponseEntity.ok(boardPhotoService.list(boardId));
  }

  @PostMapping(value = "/posts/{boardId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<BoardPhotoDto>> upload(
      @PathVariable("boardId") Long boardId,
      @RequestPart("photos") List<MultipartFile> photos,
      HttpSession session
  ) {
    Long memberId = getLoginMemberId(session);
    return ResponseEntity.ok(boardPhotoService.upload(boardId, memberId, photos));
  }

  // ✅ img src로 바로 쓰는 용도
  @GetMapping("/photos/{photoId}/view")
  public ResponseEntity<Resource> view(@PathVariable("photoId") Long photoId) throws Exception {
    Resource resource = boardPhotoService.view(photoId);

    // content-type 추정 (jpg/png/gif 등)
    String contentType = "image/*";
    try {
      Path p = Paths.get(resource.getFile().getAbsolutePath());
      String probed = Files.probeContentType(p);
      if (probed != null) contentType = probed;
    } catch (Exception ignored) {}

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
        .body(resource);
  }

  @DeleteMapping("/photos/{photoId}")
  public ResponseEntity<Void> delete(@PathVariable("photoId") Long photoId, HttpSession session) {
    Long memberId = getLoginMemberId(session);
    boardPhotoService.delete(photoId, memberId);
    return ResponseEntity.ok().build();
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
