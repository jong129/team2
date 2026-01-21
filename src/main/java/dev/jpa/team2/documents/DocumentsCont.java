package dev.jpa.team2.documents;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.client.RestClientException;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

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
  DocumentReportService documentReportService;

  @Autowired
  private LLMRequestConfig llmRequestConfig;

  @PostMapping("/analyze")
  public ResponseEntity<String> analyze(@ModelAttribute DocumentsDTO documentsDTO) {

    MultipartFile mf = documentsDTO.getFile1MF();
    Number userId = documentsDTO.getUserId();

    log.info("문서 분석 요청 수신 userId={}", userId);

    if (mf == null || mf.isEmpty()) {
      throw new BusinessException(ErrorCode.FILE_MISSING);
    }
    if (userId == null) {
      throw new BusinessException(ErrorCode.FILE_MISSING, "userId가 없습니다. (요청에 userId가 포함되지 않았습니다)");
    }

    String response = null;

    try {
      // 1) 파일 저장
      String upDir = Tool.getServerDir("documents");
      String savedFilename = Upload.saveFile(mf, upDir);
      documentsDTO.setFilePath(savedFilename);
      // (중요) 파일명/경로를 DB에 남길 필드가 있으면 DTO에 세팅해두기
      // documentsDTO.setFile1(savedFilename); // 필드명 프로젝트에 맞게

      // 2) FastAPI 호출
      String url = "http://121.160.42.81:8000/document/analyze";
      log.info("[FASTAPI] 요청 시작 url={}, image_path={}", url, savedFilename);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> body = new HashMap<>();
      body.put("SpringBoot_FastAPI_KEY", llmRequestConfig.getSpringBoot_FastAPI_KEY());
      body.put("userId", userId);
      body.put("image_path", savedFilename);

      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      response = llmRequestConfig.getRestTemplate().postForObject(url, requestEntity, String.class);
      log.info("[FASTAPI] 응답 수신 (length={})", response == null ? 0 : response.length());

      // 3) ✅ JSON 파싱 (파싱 실패는 여기서만)
      ObjectMapper om = new ObjectMapper();
      JsonNode root;
      try {
        root = om.readTree(response);
      } catch (Exception e) {
        log.error("❌ JSON 파싱 실패 raw={}", response, e);
        throw new BusinessException(ErrorCode.FASTAPI_ERROR, "AI 응답 JSON 파싱 실패", e);
      }

  

      String docType = root.path("doc_type").asText("UNKNOWN");
      String policyVersion = root.path("policy_version").asText("unknown");
      int riskScore = root.path("risk_score").asInt(0);

      JsonNode reasonsNode = root.path("reasons");
      String reasonsJson = (reasonsNode.isMissingNode() || reasonsNode.isNull()) ? "[]"
          : om.writeValueAsString(reasonsNode);

      JsonNode parsedNode = root.path("parsed_data");
      String parsedJson = (parsedNode.isMissingNode() || parsedNode.isNull()) ? "{}"
          : om.writeValueAsString(parsedNode);

      JsonNode aiNode = root.path("ai_explanation");
      String aiExplanation = (aiNode.isMissingNode() || aiNode.isNull()) ? null : aiNode.asText();
      
      // ✅ 파싱 성공 로그
      log.info("✅ PARSE OK => docType={}, policyVersion={}, riskScore={}", docType, policyVersion, riskScore);

      // 4) ✅ 문서 저장 먼저! (시퀀스 docId 생성)
      documentsDTO.setDocType(docType);

      Documents savedDoc = documentsService.save(documentsDTO);
      Long docId = savedDoc.getDocId();

      log.info("✅ Documents 저장 OK => docId={}", docId);

      // 5) ✅ report 저장 (docId 필수 세팅)
      DocumentReportDTO reportDTO = new DocumentReportDTO();
      reportDTO.setDocId(docId);
      reportDTO.setDocType(docType);
      reportDTO.setPolicyVersion(policyVersion);
      reportDTO.setRiskScore(riskScore);
      reportDTO.setReasonsJson(reasonsJson);
      reportDTO.setParsedJson(parsedJson);
      reportDTO.setAiExplanation(aiExplanation);

      if (!"UNKNOWN".equals(docType)) {
        try {
          documentReportService.save(reportDTO);
          log.info("✅ DocumentReport 저장 OK => docId={}", docId);
        } catch (Exception e) {
          log.error("❌ DocumentReport 저장 실패(파싱은 성공) docId={}", docId, e);
          throw e;
        }
      } else {
        log.info("ℹ️ Report 저장 스킵 docType={}", docType);
      }
      String fileNameOnly = Paths.get(savedFilename).getFileName().toString();
      ((com.fasterxml.jackson.databind.node.ObjectNode) root)
      .put("image_path", "/files/" + fileNameOnly);
      String finalResponse = om.writeValueAsString(root);
      log.info(finalResponse);
      return ResponseEntity.ok(finalResponse);

    } catch (RestClientException e) {
      throw new BusinessException(ErrorCode.FASTAPI_ERROR, "FastAPI 호출 실패: " + e.getMessage(), e);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("❌ 처리 중 예외 발생 raw={}", response, e);
      throw new BusinessException(ErrorCode.FASTAPI_ERROR, "문서 분석 처리 중 오류", e);
    } finally {
      MDC.clear();
    }
  }
  @GetMapping("/show")
  public ResponseEntity<List<AdminDocumentViewDTO>> show(@RequestParam("userId") Long userId) {
    return ResponseEntity.ok(documentsService.showByUserId(userId));
  }
  

  @DeleteMapping("/delete/{docId}/report")
  public ResponseEntity<Void> deleteReport(@PathVariable("docId") Long docId) {
      documentsService.deleteReportByDocId(docId);
      
      return ResponseEntity.noContent().build();
  }
  @DeleteMapping("/delete/{docId}/document")
  public ResponseEntity<Void> deleteDocument(@PathVariable("docId") Long docId) {
      documentsService.deleteDocumentByDocId(docId);
      log.info("docId",docId);
      return ResponseEntity.noContent().build();
  }
}
