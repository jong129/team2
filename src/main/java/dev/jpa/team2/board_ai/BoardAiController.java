package dev.jpa.team2.board_ai;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/board/ai")
public class BoardAiController {

    private final BoardAiService boardAiService;

    public BoardAiController(BoardAiService boardAiService) {
        this.boardAiService = boardAiService;
    }

    private void requireLogin(HttpSession session) {
        Object memberId = session.getAttribute("LOGIN_MEMBER_ID");
        if (memberId == null) throw new ResponseStatusException(UNAUTHORIZED, "login required");
    }

 // LLM #1: 요약
    @PostMapping("/summary/{boardId}")
    public AiResultResponse summary(@PathVariable("boardId") Long boardId,
                                    @RequestBody(required = false) AiGenerateRequest req,
                                    HttpSession session) {
        requireLogin(session);
        if (req == null) req = new AiGenerateRequest();
        return boardAiService.generateSummary(boardId, req);
    }

    // LLM #2: 호재/악재(긍/부정) 해석
    @PostMapping("/sentiment/{boardId}")
    public AiResultResponse sentiment(@PathVariable("boardId") Long boardId,
                                      @RequestBody(required = false) AiGenerateRequest req,
                                      HttpSession session) {
        requireLogin(session);
        if (req == null) req = new AiGenerateRequest();
        return boardAiService.analyzeSentiment(boardId, req);
    }
    
    @PostMapping("/write/{categoryId}")
    public AiWriteDraftResponse writeDraft(
        @PathVariable("categoryId") Long categoryId,
        @RequestBody(required = false) AiWriteDraftRequest req,
        HttpSession session
    ) {
      requireLogin(session);
      if (req == null) req = new AiWriteDraftRequest();
      return boardAiService.generateWriteDraft(categoryId, req);
    }


}
