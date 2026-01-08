package dev.jpa.team2.board.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board/categories")
public class BoardCategoryController {

  private final BoardCategoryService boardCategoryService;

  @GetMapping("/list")
  public ResponseEntity<List<BoardCategoryDto>> list() {
    return ResponseEntity.ok(boardCategoryService.publicList());
  }
}
