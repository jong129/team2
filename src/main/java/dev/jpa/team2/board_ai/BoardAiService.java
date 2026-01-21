package dev.jpa.team2.board_ai;

public interface BoardAiService {
    AiResultResponse generateSummary(Long boardId, AiGenerateRequest req);
    AiResultResponse analyzeSentiment(Long boardId, AiGenerateRequest req);
    AiWriteDraftResponse generateWriteDraft(Long categoryId, Long memberId, AiWriteDraftRequest req);
}
