package dev.jpa.team2.board.file;

import dev.jpa.team2.board.BoardFileDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardFileController {

  private final BoardFileService boardFileService;

  @GetMapping("/posts/{boardId}/files")
  public ResponseEntity<List<BoardFileDto>> list(@PathVariable("boardId") Long boardId) {
    return ResponseEntity.ok(boardFileService.list(boardId));
  }

  @PostMapping(value = "/posts/{boardId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<BoardFileDto>> upload(
      @PathVariable("boardId") Long boardId,
      @RequestPart("files") List<MultipartFile> files,
      HttpSession session
  ) {
    Long memberId = getLoginMemberId(session);
    return ResponseEntity.ok(boardFileService.upload(boardId, memberId, files));
  }

  @GetMapping("/files/{fileId}/download")
  public ResponseEntity<Resource> download(@PathVariable("fileId") Long fileId) {
    Resource resource = boardFileService.download(fileId);
    BoardFile meta = boardFileService.getFileMeta(fileId);

    String encoded;
    try {
      encoded = java.net.URLEncoder.encode(meta.getOriginalName(), java.nio.charset.StandardCharsets.UTF_8)
          .replaceAll("\\+", "%20");
    } catch (Exception e) {
      encoded = "download";
    }

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        // ✅ 한글 파일명 대응
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
        .body(resource);
  }
  
  @DeleteMapping("/files/{fileId}")
  public ResponseEntity<Void> delete(
      @PathVariable("fileId") Long fileId,
      HttpSession session
  ) {
    Long memberId = getLoginMemberId(session);
    boardFileService.delete(fileId, memberId);
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
