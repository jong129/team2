package dev.jpa.team2.board_ai;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/board/ai")
public class BoardAiController {

    private final BoardAiService boardAiService;

    public BoardAiController(BoardAiService boardAiService) {
        this.boardAiService = boardAiService;
    }

    // 로그인 체크 + memberId 반환
    private Long requireLogin(HttpSession session) {
        Object memberIdObj = session.getAttribute("LOGIN_MEMBER_ID");
        if (memberIdObj == null) throw new ResponseStatusException(UNAUTHORIZED, "login required");

        if (memberIdObj instanceof Long) return (Long) memberIdObj;
        return Long.valueOf(String.valueOf(memberIdObj));
    }

    @PostMapping("/summary/{boardId}")
    public AiResultResponse summary(@PathVariable("boardId") Long boardId,
                                    @RequestBody(required = false) AiGenerateRequest req,
                                    HttpSession session) {
        requireLogin(session);
        if (req == null) req = new AiGenerateRequest();
        return boardAiService.generateSummary(boardId, req);
    }

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
            @RequestBody AiWriteDraftRequest req,
            HttpSession session
    ) {
        Long memberId = requireLogin(session);
        // title/content 둘 다 비면 400으로 컷
        if ((req.getTitle() == null || req.getTitle().trim().isEmpty()) &&
            (req.getContent() == null || req.getContent().trim().isEmpty())) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "title/content 둘 중 하나는 필요합니다."
            );
        }
        return boardAiService.generateWriteDraft(categoryId, memberId, req);
    }

}

