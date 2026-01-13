package dev.jpa.team2.board.file;

import dev.jpa.team2.board.BoardFileDto;
import dev.jpa.team2.board.BoardPost;
import dev.jpa.team2.board.BoardPostRepository;
import dev.jpa.team2.board.category.BoardCategory;
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
public class BoardFileService {

  private final BoardFileRepository boardFileRepository;
  private final BoardPostRepository boardPostRepository;
  private final MemberRoleService memberRoleService;

  @Value("${board.file.dir:uploads/board/files}")
  private String baseDir;

  @Transactional(readOnly = true)
  public List<BoardFileDto> list(Long boardId) {
    BoardPost post = getPost(boardId);
    requireEnabled(post.getCategory().getFileYn(), "file");

    return boardFileRepository.findByBoardIdOrderByFileIdAsc(boardId).stream().map(BoardFileDto::from)
        .collect(Collectors.toList());
  }

  public List<BoardFileDto> upload(Long boardId, Long loginMemberId, List<MultipartFile> files) {
    requireLogin(loginMemberId);

    BoardPost post = getPost(boardId);
    BoardCategory category = post.getCategory();
    requireEnabled(category.getFileYn(), "file");
    requireOwnerOrAdmin(post, loginMemberId);

    if (files == null || files.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "files required");
    }

    Path dir = Paths.get(baseDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(dir);
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "cannot create upload dir");
    }

    List<BoardFileDto> savedList = new ArrayList<>();

    for (MultipartFile mf : files) {
      if (mf == null || mf.isEmpty())
        continue;

      String original = safeOriginalName(mf.getOriginalFilename());
      String saved = makeSavedName(original);

      Path target = dir.resolve(saved).normalize();

      try {
        mf.transferTo(target);
      } catch (IOException e) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "file save failed");
      }

      BoardFile e = new BoardFile();
      e.setBoardId(boardId);
      e.setOriginalName(original);
      e.setSavedName(saved);
      e.setFileSize(mf.getSize());

      BoardFile savedEntity = boardFileRepository.save(e);
      savedList.add(BoardFileDto.from(savedEntity));
    }

    return savedList;
  }

  @Transactional(readOnly = true)
  public Resource download(Long fileId) {
    BoardFile f = boardFileRepository.findById(fileId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "file not found"));

    Path dir = Paths.get(baseDir).toAbsolutePath().normalize();
    Path path = dir.resolve(f.getSavedName()).normalize();

    if (!Files.exists(path)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file missing on server");
    }

    try {
      return new UrlResource(path.toUri());
    } catch (MalformedURLException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "file url error");
    }
  }

  @Transactional(readOnly = true)
  public BoardFile getFileMeta(Long fileId) {
    return boardFileRepository.findById(fileId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "file not found"));
  }

  public void delete(Long fileId, Long loginMemberId) {
    requireLogin(loginMemberId);

    BoardFile f = boardFileRepository.findById(fileId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "file not found"));

    BoardPost post = getPost(f.getBoardId());
    requireEnabled(post.getCategory().getFileYn(), "file");
    requireOwnerOrAdmin(post, loginMemberId);

    // 1) 실제 파일 삭제 시도(없어도 진행)
    Path dir = Paths.get(baseDir).toAbsolutePath().normalize();
    Path path = dir.resolve(f.getSavedName()).normalize();
    try {
      Files.deleteIfExists(path);
    } catch (Exception ignored) {
    }

    // 2) DB row 하드삭제
    boardFileRepository.deleteById(fileId);
  }

  private BoardPost getPost(Long boardId) {
    return boardPostRepository.findByBoardIdAndDeletedYn(boardId, "N")
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));
  }

  private void requireEnabled(String yn, String featureName) {
    if (!"Y".equalsIgnoreCase(yn)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, featureName + " disabled");
    }
  }

  private void requireLogin(Long memberId) {
    if (memberId == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
  }

  private void requireOwnerOrAdmin(BoardPost post, Long loginMemberId) {
    boolean isAdmin = memberRoleService.isAdmin(loginMemberId);
    boolean isOwner = loginMemberId.equals(post.getMemberId());
    if (!isAdmin && !isOwner)
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no permission");
  }

  private String safeOriginalName(String name) {
    String x = (name == null ? "file" : name.trim());
    x = x.replace("\\", "_").replace("/", "_");
    if (x.isEmpty())
      x = "file";
    return x;
  }

  private String makeSavedName(String original) {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    long ts = System.currentTimeMillis();
    return ts + "_" + uuid + "_" + original;
  }
}
