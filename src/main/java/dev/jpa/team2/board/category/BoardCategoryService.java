package dev.jpa.team2.board.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardCategoryService {

  private final BoardCategoryRepository boardCategoryRepository;

  public BoardCategoryDto create(BoardCategoryCreateRequest req) {
    String name = normalize(req.getCategoryName());
    if (name.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryName is required");
    if (boardCategoryRepository.existsByCategoryName(name)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "categoryName already exists");
    }

    BoardCategory e = new BoardCategory();
    e.setCategoryName(name);

    applyOptionalFields(e, req.getVisibleYn(), req.getWritePolicy(),
        req.getCommentYn(), req.getReportYn(), req.getLikeYn(), req.getSecretYn(), req.getFileYn(), req.getSortNo());

    return BoardCategoryDto.fromEntity(boardCategoryRepository.save(e));
  }

  public BoardCategoryDto update(Long categoryId, BoardCategoryUpdateRequest req) {
    BoardCategory e = boardCategoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found"));

    if (req.getCategoryName() != null) {
      String name = normalize(req.getCategoryName());
      if (name.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryName is empty");

      if (!name.equals(e.getCategoryName()) && boardCategoryRepository.existsByCategoryName(name)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "categoryName already exists");
      }
      e.setCategoryName(name);
    }

    applyOptionalFields(e, req.getVisibleYn(), req.getWritePolicy(),
        req.getCommentYn(), req.getReportYn(), req.getLikeYn(), req.getSecretYn(), req.getFileYn(), req.getSortNo());

    return BoardCategoryDto.fromEntity(e);
  }

  public void delete(Long categoryId) {
    if (!boardCategoryRepository.existsById(categoryId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found");
    }
    boardCategoryRepository.deleteById(categoryId);
  }

  @Transactional(readOnly = true)
  public List<BoardCategoryDto> adminList() {
    return boardCategoryRepository.findAllByOrderBySortNoAscCategoryIdAsc()
        .stream().map(BoardCategoryDto::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public List<BoardCategoryDto> publicList() {
    return boardCategoryRepository.findAllByVisibleYnOrderBySortNoAscCategoryIdAsc("Y")
        .stream().map(BoardCategoryDto::fromEntity).toList();
  }

  private void applyOptionalFields(
      BoardCategory e,
      String visibleYn,
      BoardCategoryWritePolicy writePolicy,
      String commentYn,
      String reportYn,
      String likeYn,
      String secretYn,
      String fileYn,
      Integer sortNo
  ) {
    if (visibleYn != null) e.setVisibleYn(assertYn(visibleYn, "visibleYn"));
    if (writePolicy != null) e.setWritePolicy(writePolicy);

    if (commentYn != null) e.setCommentYn(assertYn(commentYn, "commentYn"));
    if (reportYn != null) e.setReportYn(assertYn(reportYn, "reportYn"));
    if (likeYn != null) e.setLikeYn(assertYn(likeYn, "likeYn"));
    if (secretYn != null) e.setSecretYn(assertYn(secretYn, "secretYn"));
    if (fileYn != null) e.setFileYn(assertYn(fileYn, "fileYn"));

    if (sortNo != null) e.setSortNo(sortNo);
  }

  private String normalize(String s) {
    return s == null ? "" : s.trim();
  }

  private String assertYn(String v, String field) {
    String x = v.trim().toUpperCase();
    if (!x.equals("Y") && !x.equals("N")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " must be Y or N");
    }
    return x;
  }
}
