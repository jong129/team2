package dev.jpa.team2.board.photo;

import dev.jpa.team2.board.BoardPhotoDto;
import dev.jpa.team2.board.BoardPost;
import dev.jpa.team2.board.BoardPostRepository;
import dev.jpa.team2.member.member_role.MemberRoleService;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardPhotoService {

  private final BoardPhotoRepository boardPhotoRepository;
  private final BoardPostRepository boardPostRepository;
  private final MemberRoleService memberRoleService;

  @Value("${board.photo.dir:uploads/board/photos}")
  private String baseDir;

  @Transactional(readOnly = true)
  public List<BoardPhotoDto> list(Long boardId) {
    getPost(boardId); // 글 존재 확인
    return boardPhotoRepository.findByBoardIdOrderByPhotoIdAsc(boardId)
        .stream().map(BoardPhotoDto::from).collect(Collectors.toList());
  }

  public List<BoardPhotoDto> upload(Long boardId, Long loginMemberId, List<MultipartFile> photos) {
    requireLogin(loginMemberId);

    BoardPost post = getPost(boardId);
    requireOwnerOrAdmin(post, loginMemberId);

    if (photos == null || photos.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "photos required");
    }

    Path dir = Paths.get(baseDir).toAbsolutePath().normalize();
    try { Files.createDirectories(dir); }
    catch (IOException e) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "cannot create upload dir"); }

    List<BoardPhotoDto> saved = new ArrayList<>();

    for (MultipartFile mf : photos) {
      if (mf == null || mf.isEmpty()) continue;

      String original = safeName(mf.getOriginalFilename());
      String savedName = makeSavedName(original);

      Path target = dir.resolve(savedName).normalize();
      try { mf.transferTo(target); }
      catch (IOException e) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "photo save failed"); }

      BoardPhoto p = new BoardPhoto();
      p.setBoardId(boardId);
      p.setSavedName(savedName);
      // thumbName은 일단 미사용(null)

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

    if (!Files.exists(path)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "photo missing on server");

    try { return new UrlResource(path.toUri()); }
    catch (MalformedURLException e) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "photo url error"); }
  }

  public void delete(Long photoId, Long loginMemberId) {
    requireLogin(loginMemberId);

    BoardPhoto p = boardPhotoRepository.findById(photoId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "photo not found"));

    BoardPost post = getPost(p.getBoardId());
    requireOwnerOrAdmin(post, loginMemberId);

    Path dir = Paths.get(baseDir).toAbsolutePath().normalize();
    Path path = dir.resolve(p.getSavedName()).normalize();
    try { Files.deleteIfExists(path); } catch (Exception ignored) {}

    boardPhotoRepository.deleteById(photoId); // 하드삭제
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
}
