package dev.jpa.team2.documents;

//Spring Web
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestClientException;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;

//Java
import java.util.Map;
import java.util.HashMap;

import dev.jpa.team2.tool.BusinessException;
import dev.jpa.team2.tool.ErrorCode;
import dev.jpa.team2.tool.LLMRequestConfig;
import dev.jpa.team2.tool.Tool;
import dev.jpa.team2.tool.Upload;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/documents")
public class DocumentsCont {
  @Autowired
  DocumentsService documentsService;
  @Autowired
  private LLMRequestConfig llmRequestConfig;
  
  @PostMapping("/analyze")
  public ResponseEntity<String> analyze(@ModelAttribute DocumentsDTO documentsDTO) {


      MultipartFile mf = documentsDTO.getFile1MF();
      Number userId = documentsDTO.getUserId();

      log.info("문서 분석 요청 수신 userId={}", userId);
      log.info("문서 분석 요청 수신");

      if (mf == null || mf.isEmpty()) {
          throw new BusinessException(ErrorCode.FILE_MISSING);
      }

      try {
          String upDir = Tool.getServerDir("documents");
          String savedFilename = Upload.saveFile(mf, upDir);

          String url = "http://121.160.42.81:8000/document/analyze";
          log.info("[FASTAPI] 요청 시작 url={}", url);

          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_JSON);

          Map<String, Object> body = new HashMap<>();
          body.put("SpringBoot_FastAPI_KEY", llmRequestConfig.getSpringBoot_FastAPI_KEY());
          body.put("userId", userId);
          body.put("image_path", savedFilename);

          HttpEntity<Map<String, Object>> requestEntity =
                  new HttpEntity<>(body, headers);

          String response = llmRequestConfig.getRestTemplate()
                  .postForObject(url, requestEntity, String.class);

          log.info("[FASTAPI] 응답 수신");
          documentsService.save(documentsDTO);
          return ResponseEntity.ok(response);

      } catch (RestClientException e) {
          throw new BusinessException(
                  ErrorCode.FASTAPI_ERROR,
                  "FastAPI 호출 실패: " + e.getMessage(),
                  e
          );

      } finally {
          MDC.clear();
      }
  }
}
