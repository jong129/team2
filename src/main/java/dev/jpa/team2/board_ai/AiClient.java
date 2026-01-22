package dev.jpa.team2.board_ai;

public interface AiClient {
  PythonAiResponse summarize(String prompt, String title, String content);
  PythonAiResponse sentiment(String prompt, String title, String content);
  PythonAiResponse writeDraft(String prompt, String title, String content);
  PythonAiResponse moderateImage(String prompt, String imageBase64, String filename, String contentType);
}
