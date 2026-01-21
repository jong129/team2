package dev.jpa.team2.board.photo;

import dev.jpa.team2.board_ai.AiClient;
import dev.jpa.team2.board_ai.AiPrompt;
import dev.jpa.team2.board_ai.AiPromptRepository;
import dev.jpa.team2.board_ai.PythonAiResponse;
import dev.jpa.team2.tool.BusinessException;
import dev.jpa.team2.tool.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BoardImageModerationService {

  private final BoardImageBlockLogWriter blockLogWriter; // ✅ REQUIRES_NEW 저장
  private final AiPromptRepository aiPromptRepository;
  private final AiClient aiClient;

  private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");
  private static final long MAX_BYTES = 5L * 1024 * 1024;

  /**
   * ✅ precheck / upload 공용: 예외 던지지 않고 판별 결과만 리턴
   * - 차단이면 DB 로그 남기고 allowed=false 리턴
   * - AI 서버 오류도 정책상 "차단"으로 보고 로그 남길 수 있음
   */
  public ModerationResult check(Long boardId, Long memberId, MultipartFile mf) {
    String original = safe(mf.getOriginalFilename());
    String contentType = mf.getContentType();
    long size = Math.max(0, mf.getSize());

    // 1) 빈 파일
    if (mf == null || mf.isEmpty() || size == 0) {
      return blocked(boardId, memberId, original, contentType, size,
          "FILE_EMPTY", "빈 파일은 업로드할 수 없습니다.", null, null, null);
    }

    // 2) 용량 제한
    if (size > MAX_BYTES) {
      return blocked(boardId, memberId, original, contentType, size,
          "FILE_TOO_LARGE", "파일 용량이 너무 큽니다.", null, null, null);
    }

    // 3) 이미지 타입/확장자 체크
    boolean isImage = contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/");
    String ext = getExt(original);

    if (!isImage && (ext == null || !ALLOWED_EXT.contains(ext))) {
      return blocked(boardId, memberId, original, contentType, size,
          "INVALID_FILE_TYPE", "이미지 파일만 업로드할 수 있습니다.", null, null, null);
    }

    // 4) AI 판별
    AiPrompt prompt = aiPromptRepository
        .findTopByAiTypeAndUseYnOrderByCreatedAtDesc("IMAGE_MODERATION", "Y")
        .orElseThrow(() -> new BusinessException(
            ErrorCode.FASTAPI_ERROR, "AI prompt not found for IMAGE_MODERATION"));

    ModerationResult r;
    try {
      r = callModeration(prompt.getPromptText(), mf, original, contentType);
    } catch (Exception e) {
      // ✅ AI 서버 오류도 '차단'으로 처리(보수적)
      return blocked(boardId, memberId, original, contentType, size,
          "AI_ERROR", "이미지 판별 서버 오류로 업로드할 수 없습니다.",
          prompt.getPromptCode(), null, null);
    }

    r.promptCode = prompt.getPromptCode();

    if (!r.allowed) {
      return blocked(boardId, memberId, original, contentType, size,
          nvl(r.reasonCode, "OTHER"),
          nvl(r.reasonText, "부적절한 이미지"),
          prompt.getPromptCode(),
          r.rawJson,
          r.score
      );
    }

    return r; // allowed=true
  }

  /**
   * ✅ upload(실제 저장) 단계에서 사용:
   * - 차단이면 예외 던져서 업로드 자체가 중단되게 함
   */
  public void checkOrThrow(Long boardId, Long memberId, MultipartFile mf) {
    ModerationResult r = check(boardId, memberId, mf);
    if (!r.allowed) {
      // 프론트에서 reasonText 보여주고 싶으면 메시지에 포함시키면 됨
      throw new BusinessException(ErrorCode.IMAGE_REJECTED, nvl(r.reasonText, "부적절한 이미지"));
    }
  }

  private ModerationResult callModeration(String promptText, MultipartFile mf, String original, String contentType) throws Exception {
    byte[] bytes = mf.getBytes();
    String b64 = Base64.getEncoder().encodeToString(bytes);

    PythonAiResponse res = aiClient.moderateImage(promptText, b64, original, contentType);
    String json = res.getResultText(); // FastAPI는 JSON만 내려주는 전제

    return ModerationResult.fromJson(json);
  }

  private ModerationResult blocked(
      Long boardId,
      Long memberId,
      String originalName,
      String contentType,
      long size,
      String reasonCode,
      String reasonText,
      String promptCode,
      String rawResult,
      Double score
  ) {
    // ✅ DB 로그 저장 (REQUIRES_NEW)
    BoardImageBlockLog log = new BoardImageBlockLog();
    log.setBoardId(boardId);          // ✅ precheck는 null로 저장
    log.setMemberId(memberId);
    log.setOriginalName(originalName);
    log.setContentType(contentType);
    log.setFileSize(size);
    log.setAllowedYn("N");
    log.setReasonCode(reasonCode);
    log.setReasonText(reasonText);
    log.setAiType("IMAGE_MODERATION");
    log.setPromptCode(promptCode);
    log.setRawResult(rawResult);

    blockLogWriter.save(log);

    ModerationResult r = new ModerationResult();
    r.allowed = false;
    r.reasonCode = reasonCode;
    r.reasonText = reasonText;
    r.promptCode = promptCode;
    r.rawJson = rawResult;
    r.score = score;
    return r;
  }

  private String safe(String s) {
    if (s == null) return "file";
    String x = s.trim().replace("\\", "_").replace("/", "_");
    return x.isEmpty() ? "file" : x;
  }

  private String getExt(String filename) {
    if (filename == null) return null;
    int idx = filename.lastIndexOf('.');
    if (idx < 0 || idx == filename.length() - 1) return null;
    return filename.substring(idx + 1).toLowerCase(Locale.ROOT);
  }

  private String nvl(String v, String def) {
    return (v == null || v.isBlank()) ? def : v;
  }

  // =========================
  // 결과 객체
  // =========================
  public static class ModerationResult {
    public boolean allowed;
    public String reasonCode;
    public String reasonText;
    public String promptCode;
    public String rawJson;
    public Double score;

    /**
     * FastAPI JSON 스키마:
     * {"allowed":true|false,"reason_code":"...","reason_text":"...","score":0.0}
     */
    public static ModerationResult fromJson(String json) {
      ModerationResult r = new ModerationResult();
      r.rawJson = json;

      String j = (json == null ? "" : json.trim());

      r.allowed = containsTrue(j, "\"allowed\"");
      r.reasonCode = pickString(j, "reason_code");
      r.reasonText = pickString(j, "reason_text");
      r.score = pickNumber(j, "score");
      return r;
    }

    private static boolean containsTrue(String json, String key) {
      int idx = json.indexOf(key);
      if (idx < 0) return true;
      int colon = json.indexOf(':', idx);
      if (colon < 0) return true;
      String tail = json.substring(colon + 1).trim();
      return tail.startsWith("true");
    }

    private static String pickString(String json, String key) {
      String k = "\"" + key + "\"";
      int idx = json.indexOf(k);
      if (idx < 0) return null;
      int colon = json.indexOf(':', idx);
      if (colon < 0) return null;
      int firstQuote = json.indexOf('"', colon + 1);
      if (firstQuote < 0) return null;
      int secondQuote = json.indexOf('"', firstQuote + 1);
      if (secondQuote < 0) return null;
      return json.substring(firstQuote + 1, secondQuote);
    }

    private static Double pickNumber(String json, String key) {
      String k = "\"" + key + "\"";
      int idx = json.indexOf(k);
      if (idx < 0) return null;
      int colon = json.indexOf(':', idx);
      if (colon < 0) return null;

      int start = colon + 1;
      while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

      int end = start;
      while (end < json.length()) {
        char c = json.charAt(end);
        if (!(Character.isDigit(c) || c == '.' || c == '-' || c == '+')) break;
        end++;
      }

      if (end <= start) return null;

      try {
        return Double.parseDouble(json.substring(start, end));
      } catch (Exception e) {
        return null;
      }
    }
  }
}

