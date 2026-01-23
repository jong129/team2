package dev.jpa.team2.board.photo;

import dev.jpa.team2.board.BoardPhotoDto;
import dev.jpa.team2.board.BoardPost;
import dev.jpa.team2.board.BoardPostRepository;
import dev.jpa.team2.member.member_role.MemberRoleService;
import dev.jpa.team2.tool.BusinessException;
import dev.jpa.team2.tool.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardPhotoService {

  private final BoardPhotoRepository boardPhotoRepository;
  private final BoardPostRepository boardPostRepository;
  private final MemberRoleService memberRoleService;
  private final BoardImageModerationService boardImageModerationService;
  private final BoardPhotoPrecheckCache precheckCache;

  @Value("${board.photo.dir:uploads/board/photos}")
  private String baseDir;

  private String sha256(MultipartFile mf) {
    try {
      byte[] bytes = mf.getBytes();
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] dig = md.digest(bytes);
      StringBuilder sb = new StringBuilder();
      for (byte b : dig) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "hash failed");
    }
  }

  @Transactional(readOnly = true)
  public List<BoardPhotoDto> list(Long boardId) {
    getPost(boardId);
    return boardPhotoRepository.findByBoardIdOrderByPhotoIdAsc(boardId)
        .stream()
        .map(BoardPhotoDto::from)
        .collect(Collectors.toList());
  }

  public List<BoardPhotoDto> upload(Long boardId, Long loginMemberId, List<MultipartFile> photos) {
    requireLogin(loginMemberId);

    BoardPost post = getPost(boardId);
    requireOwnerOrAdmin(post, loginMemberId);

    if (photos == null || photos.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "photos required");
    }

    Path dir = Paths.get(baseDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(dir);
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "cannot create upload dir");
    }

    List<BoardPhotoDto> saved = new ArrayList<>();

    for (MultipartFile mf : photos) {
      if (mf == null || mf.isEmpty()) continue;

      // ✅ A안 핵심: precheck 캐시 히트면 AI 재호출 스킵
      String hash = sha256(mf);
      BoardPhotoPrecheckCache.CacheValue cached = precheckCache.get(loginMemberId, hash);

      if (cached != null) {
        if (!cached.allowed()) {
          throw new BusinessException(
              ErrorCode.IMAGE_REJECTED,
              (cached.reasonText() == null || cached.reasonText().isBlank()) ? "부적절한 이미지" : cached.reasonText()
          );
        }
        // allowed=true면 통과(추가 AI 호출 없음)
      } else {
        // 캐시 미스면 보안상 재검증
        boardImageModerationService.checkOrThrow(boardId, loginMemberId, mf);
      }

      String original = safeName(mf.getOriginalFilename());
      String savedName = makeSavedName(original);

      Path target = dir.resolve(savedName).normalize();
      try {
        mf.transferTo(target);
      } catch (IOException e) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "photo save failed");
      }

      BoardPhoto p = new BoardPhoto();
      p.setBoardId(boardId);
      p.setSavedName(savedName);

      saved.add(BoardPhotoDto.from(boardPhotoRepository.save(p)));
    }

    return saved;
  }

  @Transactional(readOnly = true)
  public Resource view(Long photoId) {
    BoardPhoto p = boardPhotoRepository.findById(photoId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "photo not found"));

    Path dir = Paths.get(baseDir).toAbsolutePath().normalize();
    Path path = dir.resolve(p.getSavedName()).normalize();

    if (!Files.exists(path)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "photo missing on server");
    }

    try {
      return new UrlResource(path.toUri());
    } catch (MalformedURLException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "photo url error");
    }
  }

  public void delete(Long photoId, Long loginMemberId) {
    requireLogin(loginMemberId);

    BoardPhoto p = boardPhotoRepository.findById(photoId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "photo not found"));

    BoardPost post = getPost(p.getBoardId());
    requireOwnerOrAdmin(post, loginMemberId);

    Path dir = Paths.get(baseDir).toAbsolutePath().normalize();
    Path path = dir.resolve(p.getSavedName()).normalize();
    try {
      Files.deleteIfExists(path);
    } catch (Exception ignored) {}

    boardPhotoRepository.deleteById(photoId);
  }

  private BoardPost getPost(Long boardId) {
    return boardPostRepository.findByBoardIdAndDeletedYn(boardId, "N")
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));
  }

  private void requireLogin(Long memberId) {
    if (memberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
  }

  private void requireOwnerOrAdmin(BoardPost post, Long loginMemberId) {
    boolean isAdmin = memberRoleService.isAdmin(loginMemberId);
    boolean isOwner = loginMemberId.equals(post.getMemberId());
    if (!isAdmin && !isOwner) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no permission");
  }

  private String safeName(String name) {
    String x = (name == null ? "img" : name.trim());
    x = x.replace("\\", "_").replace("/", "_");
    if (x.isEmpty()) x = "img";
    return x;
  }

  private String makeSavedName(String original) {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    long ts = System.currentTimeMillis();
    return ts + "_" + uuid + "_" + original;
  }

  @Transactional(readOnly = true)
  public List<BoardPhotoPrecheckResult> precheck(Long loginMemberId, List<MultipartFile> photos) {
    requireLogin(loginMemberId);

    if (photos == null || photos.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "photos required");
    }

    List<BoardPhotoPrecheckResult> results = new ArrayList<>();

    for (MultipartFile mf : photos) {
      if (mf == null || mf.isEmpty()) continue;

      String original = safeName(mf.getOriginalFilename());
      String hash = sha256(mf);

      // ✅ precheck는 boardId가 없으므로 null
      BoardImageModerationService.ModerationResult r =
          boardImageModerationService.check(null, loginMemberId, mf);

      // ✅ 결과 캐시 저장(업로드에서 재사용)
      precheckCache.put(loginMemberId, hash, r.allowed, r.reasonCode, r.reasonText, r.score);

      results.add(new BoardPhotoPrecheckResult(
          original,
          hash,
          r.allowed,
          r.reasonCode,
          r.reasonText,
          r.score
      ));
    }

    return results;
  }
}
