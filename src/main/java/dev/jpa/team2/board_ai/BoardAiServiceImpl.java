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
  private final BoardAiDraftRepository boardAiDraftRepository; // 추가
  private final AiClient aiClient;

  public BoardAiServiceImpl(
      BoardDataReader boardDataReader,
      CategoryPolicyReader categoryPolicyReader,
      AiPromptRepository aiPromptRepository,
      BoardAiAnalysisRepository boardAiAnalysisRepository,
      BoardAiDraftRepository boardAiDraftRepository, // 추가
      AiClient aiClient
  ) {
    this.boardDataReader = boardDataReader;
    this.categoryPolicyReader = categoryPolicyReader;
    this.aiPromptRepository = aiPromptRepository;
    this.boardAiAnalysisRepository = boardAiAnalysisRepository;
    this.boardAiDraftRepository = boardAiDraftRepository;
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

    if (!req.isForce()) {
      var cached = boardAiAnalysisRepository.findTopByBoardIdAndAiTypeOrderByCreatedAtDesc(boardId, AiType.SUMMARY.name());
      if (cached.isPresent()) return toResponse(cached.get(), true);
    }

    AiPrompt prompt = aiPromptRepository
        .findTopByAiTypeAndUseYnOrderByCreatedAtDesc(AiType.SUMMARY.name(), "Y")
        .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "AI prompt not found for SUMMARY"));

    String content = normalize(board.getContent(), req.isTruncate());

    PythonAiResponse aiRes = aiClient.summarize(prompt.getPromptText(), board.getTitle(), content);
    if (aiRes.getResultText() == null || aiRes.getResultText().isBlank()) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "AI summary result empty");
    }

    BoardAiAnalysis saved = boardAiAnalysisRepository.save(
        buildAnalysis(boardId, board.getCategoryId(), AiType.SUMMARY, aiRes.getResultText(), null,
            prompt.getPromptCode(), aiRes.getModelName())
    );

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
      var cached = boardAiAnalysisRepository.findTopByBoardIdAndAiTypeOrderByCreatedAtDesc(boardId, AiType.SENTIMENT.name());
      if (cached.isPresent()) return toResponse(cached.get(), true);
    }

    AiPrompt prompt = aiPromptRepository
        .findTopByAiTypeAndUseYnOrderByCreatedAtDesc(AiType.SENTIMENT.name(), "Y")
        .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "AI prompt not found for SENTIMENT"));

    String content = normalize(board.getContent(), req.isTruncate());

    PythonAiResponse aiRes = aiClient.sentiment(prompt.getPromptText(), board.getTitle(), content);
    if (aiRes.getResultText() == null || aiRes.getResultText().isBlank()) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "AI sentiment result empty");
    }

    BoardAiAnalysis saved = boardAiAnalysisRepository.save(
        buildAnalysis(boardId, board.getCategoryId(), AiType.SENTIMENT, aiRes.getResultText(), aiRes.getScore(),
            prompt.getPromptCode(), aiRes.getModelName())
    );

    return toResponse(saved, false);
  }

  @Override
  @Transactional
  public AiWriteDraftResponse generateWriteDraft(Long categoryId, AiWriteDraftRequest req) {
    var policy = categoryPolicyReader.getPolicyOrThrow(categoryId);

    if (!"Y".equalsIgnoreCase(policy.getAiWriteYn())) {
      throw new ResponseStatusException(FORBIDDEN, "AI write disabled for this category");
    }

    String inputTitle = safe(req.getTitle());
    String inputContent = normalize(req.getContent(), true);

    // 캐시: 같은 입력(title/content) 조합에 대해 최근 결과 재사용하고 싶으면 여기서 찾으면 됨
    // 지금은 요구가 명확하지 않으니 force=false일 때만 "동일 입력 캐시"를 쓰는 패턴 추천
    if (!req.isForce()) {
      var cached = boardAiDraftRepository.findTopByCategoryIdAndInputHashOrderByCreatedAtDesc(
          categoryId, BoardAiDraft.makeInputHash(inputTitle, inputContent)
      );
      if (cached.isPresent()) {
        return toWriteResponse(cached.get(), true);
      }
    }

    AiPrompt prompt = aiPromptRepository
        .findTopByAiTypeAndUseYnOrderByCreatedAtDesc(AiType.WRITE.name(), "Y")
        .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "AI prompt not found for WRITE"));

    PythonAiResponse aiRes = aiClient.writeDraft(prompt.getPromptText(), inputTitle, inputContent);
    if (aiRes.getResultText() == null || aiRes.getResultText().isBlank()) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "AI write result empty");
    }

    BoardAiDraft saved = boardAiDraftRepository.save(
        buildDraft(categoryId, inputTitle, inputContent, aiRes.getResultText(),
            prompt.getPromptCode(), aiRes.getModelName())
    );

    return toWriteResponse(saved, false);
  }

  private String normalize(String content, boolean truncate) {
    if (content == null) content = "";
    content = content.strip();
    if (truncate && content.length() > 8000) {
      content = content.substring(0, 8000);
    }
    return content;
  }

  private String safe(String s) {
    return s == null ? "" : s.trim();
  }

  private BoardAiAnalysis buildAnalysis(Long boardId, Long categoryId, AiType type,
      String resultText, Double score, String promptCode, String modelName) {

    BoardAiAnalysis a = new BoardAiAnalysis();
    a.setBoardId(boardId);
    a.setCategoryId(categoryId);
    a.setAiType(type.name());
    a.setAiResult(resultText);
    a.setAiScore(score == null ? null : BigDecimal.valueOf(score));
    a.setPromptCode(promptCode);
    a.setModelName((modelName == null || modelName.isBlank()) ? "unknown" : modelName);
    return a;
  }

  private AiResultResponse toResponse(BoardAiAnalysis a, boolean cached) {
    Double score = (a.getAiScore() == null) ? null : a.getAiScore().doubleValue();

    return AiResultResponse.builder()
        .aiAnalysisId(a.getAiAnalysisId())
        .boardId(a.getBoardId())
        .categoryId(a.getCategoryId())
        .aiType(a.getAiType())
        .resultText(a.getAiResult())
        .score(score)
        .promptCode(a.getPromptCode())
        .modelName(a.getModelName())
        .cached(cached)
        .build();
  }

  private BoardAiDraft buildDraft(Long categoryId, String inputTitle, String inputContent,
      String resultText, String promptCode, String modelName) {

    BoardAiDraft d = new BoardAiDraft();
    d.setCategoryId(categoryId);
    d.setInputTitle(inputTitle);
    d.setInputContent(inputContent);
    d.setInputHash(BoardAiDraft.makeInputHash(inputTitle, inputContent));
    d.setAiType(AiType.WRITE.name());
    d.setAiResult(resultText);
    d.setPromptCode(promptCode);
    d.setModelName((modelName == null || modelName.isBlank()) ? "unknown" : modelName);
    return d;
  }

  private AiWriteDraftResponse toWriteResponse(BoardAiDraft d, boolean cached) {
    return AiWriteDraftResponse.builder()
        .draftId(d.getDraftId())
        .categoryId(d.getCategoryId())
        .resultText(d.getAiResult())
        .promptCode(d.getPromptCode())
        .modelName(d.getModelName())
        .cached(cached)
        .build();
  }
}
