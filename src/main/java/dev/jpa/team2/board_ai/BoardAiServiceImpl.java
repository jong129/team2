package dev.jpa.team2.board_ai;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.math.BigDecimal;

@Service
public class BoardAiServiceImpl implements BoardAiService {

  private final BoardDataReader boardDataReader;
  private final CategoryPolicyReader categoryPolicyReader;
  private final AiPromptRepository aiPromptRepository;
  private final BoardAiAnalysisRepository boardAiAnalysisRepository;
  private final AiClient aiClient;

  public BoardAiServiceImpl(BoardDataReader boardDataReader, CategoryPolicyReader categoryPolicyReader,
      AiPromptRepository aiPromptRepository, BoardAiAnalysisRepository boardAiAnalysisRepository, AiClient aiClient) {
    this.boardDataReader = boardDataReader;
    this.categoryPolicyReader = categoryPolicyReader;
    this.aiPromptRepository = aiPromptRepository;
    this.boardAiAnalysisRepository = boardAiAnalysisRepository;
    this.aiClient = aiClient;
  }

  @Override
  @Transactional
  public AiResultResponse generateSummary(Long boardId, AiGenerateRequest req) {
    var board = boardDataReader.getBoardOrThrow(boardId);
    var policy = categoryPolicyReader.getPolicyOrThrow(board.getCategoryId());

    if (!"Y".equalsIgnoreCase(policy.getAiSummaryYn())) {
      throw new ResponseStatusException(FORBIDDEN, "AI summary disabled for this category");
    }

    // 캐시 사용 (force=false면 기존 결과 재사용)
    if (!req.isForce()) {
      var cached = boardAiAnalysisRepository.findTopByBoardIdAndAiTypeOrderByCreatedAtDesc(boardId,
          AiType.SUMMARY.name());
      if (cached.isPresent())
        return toResponse(cached.get(), true);
    }

    AiPrompt prompt = aiPromptRepository.findTopByAiTypeAndUseYnOrderByCreatedAtDesc(AiType.SUMMARY.name(), "Y")
        .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "AI prompt not found for SUMMARY"));

    String content = normalize(board.getContent(), req.isTruncate());

    PythonAiResponse aiRes = aiClient.summarize(prompt.getPromptText(), board.getTitle(), content);
    if (aiRes.getResultText() == null || aiRes.getResultText().isBlank()) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "AI summary result empty");
    }

    BoardAiAnalysis saved = boardAiAnalysisRepository.save(buildAnalysis(boardId, board.getCategoryId(), AiType.SUMMARY,
        aiRes.getResultText(), null, prompt.getPromptCode(), aiRes.getModelName()));

    return toResponse(saved, false);
  }

  @Override
  @Transactional
  public AiResultResponse analyzeSentiment(Long boardId, AiGenerateRequest req) {
    var board = boardDataReader.getBoardOrThrow(boardId);
    var policy = categoryPolicyReader.getPolicyOrThrow(board.getCategoryId());

    if (!"Y".equalsIgnoreCase(policy.getAiSentimentYn())) {
      throw new ResponseStatusException(FORBIDDEN, "AI sentiment disabled for this category");
    }

    if (!req.isForce()) {
      var cached = boardAiAnalysisRepository.findTopByBoardIdAndAiTypeOrderByCreatedAtDesc(boardId,
          AiType.SENTIMENT.name());
      if (cached.isPresent())
        return toResponse(cached.get(), true);
    }

    AiPrompt prompt = aiPromptRepository.findTopByAiTypeAndUseYnOrderByCreatedAtDesc(AiType.SENTIMENT.name(), "Y")
        .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "AI prompt not found for SENTIMENT"));

    String content = normalize(board.getContent(), req.isTruncate());

    PythonAiResponse aiRes = aiClient.sentiment(prompt.getPromptText(), board.getTitle(), content);
    if (aiRes.getResultText() == null || aiRes.getResultText().isBlank()) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "AI sentiment result empty");
    }

    BoardAiAnalysis saved = boardAiAnalysisRepository
        .save(buildAnalysis(boardId, board.getCategoryId(), AiType.SENTIMENT, aiRes.getResultText(), aiRes.getScore(), // 없으면
                                                                                                                       // null
                                                                                                                       // 가능
            prompt.getPromptCode(), aiRes.getModelName()));

    return toResponse(saved, false);
  }

  private String normalize(String content, boolean truncate) {
    if (content == null)
      content = "";
    content = content.strip();

    // 토큰/지연 방지용 안전 컷
    if (truncate && content.length() > 8000) {
      content = content.substring(0, 8000);
    }
    return content;
  }

  private BoardAiAnalysis buildAnalysis(Long boardId, Long categoryId, AiType type, String resultText, Double score,
      String promptCode, String modelName) {
    BoardAiAnalysis a = new BoardAiAnalysis();
    a.setBoardId(boardId);
    a.setCategoryId(categoryId);
    a.setAiType(type.name());
    a.setAiResult(resultText);

// ✅ Double -> BigDecimal 변환 (null 안전)
    a.setAiScore(score == null ? null : BigDecimal.valueOf(score));

    a.setPromptCode(promptCode);
    a.setModelName((modelName == null || modelName.isBlank()) ? "unknown" : modelName);
    return a;
  }

  private AiResultResponse toResponse(BoardAiAnalysis a, boolean cached) {
// ✅ BigDecimal -> Double 변환 (프론트 응답은 Double로 유지)
    Double score = (a.getAiScore() == null) ? null : a.getAiScore().doubleValue();

    return AiResultResponse.builder().aiAnalysisId(a.getAiAnalysisId()).boardId(a.getBoardId())
        .categoryId(a.getCategoryId()).aiType(a.getAiType()).resultText(a.getAiResult()).score(score)
        .promptCode(a.getPromptCode()).modelName(a.getModelName()).cached(cached).build();
  }
}
