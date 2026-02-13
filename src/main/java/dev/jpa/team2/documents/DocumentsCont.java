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
import java.util.ArrayList;
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
	AnalysisService analysisService;
	@Autowired
	ReportService reportService;
	@Autowired
	RegistryService registryService;
	@Autowired
	ContractService contractService;
	@Autowired
	DocumentsService documentsService;

	@Autowired
	DocumentReportService documentReportService;

	@Autowired
	private LLMRequestConfig llmRequestConfig;

	@PostMapping("/analyze")
	public ResponseEntity<ReportDTO> analyze(@ModelAttribute AnalysisDTO anlysisDTO) {
		List<MultipartFile> mfs = anlysisDTO.getFileMFs();
		Long userId = anlysisDTO.getUserId();

		if (mfs == null || mfs.isEmpty()) {
			throw new BusinessException(ErrorCode.FILE_MISSING);
		}
		if (userId == null) {
			throw new BusinessException(ErrorCode.FILE_MISSING, "userId가 없습니다. (요청에 userId가 포함되지 않았습니다)");
		}

		String response = null;

		try {
			// 1) 파일 저장
			String upDir = Tool.getServerDir("documents");

			List<String> filePaths = new ArrayList<>();
			for (MultipartFile f : mfs) {
				String savedFilename = Upload.saveFile(f, upDir);
				filePaths.add(savedFilename);
			}
			anlysisDTO.setFilePaths(filePaths);
			// (중요) 파일명/경로를 DB에 남길 필드가 있으면 DTO에 세팅해두기
			// documentsDTO.setFile1(savedFilename); // 필드명 프로젝트에 맞게

			// 2) FastAPI 호출
			String url = "http://localhost:8000/document/analyze";
			log.info("[FASTAPI] 요청 시작 url={}, image_path={}", url);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, Object> body = new HashMap<>();
			body.put("SpringBoot_FastAPI_KEY", llmRequestConfig.getSpringBoot_FastAPI_KEY());
			body.put("userId", userId);
			body.put("image_paths", filePaths);
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			response = llmRequestConfig.getRestTemplate().postForObject(url, requestEntity, String.class);
			log.info("[FASTAPI] 응답 수신 (length={})", response == null ? 0 : response);

			// 3) ✅ JSON 파싱 (파싱 실패는 여기서만)
			ObjectMapper om = new ObjectMapper();
			JsonNode root;
			try {
				root = om.readTree(response);
			} catch (Exception e) {
				log.error("❌ JSON 파싱 실패 raw={}", response, e);
				throw new BusinessException(ErrorCode.FASTAPI_ERROR, "AI 응답 JSON 파싱 실패", e);
			}
			long count = root.path("count").asInt();
			int totalRiskScore = root.path("risk_score").asInt();
			String summary = root.path("ai_explanation").asText();
			AnalysisDTO saveAnalysisDTO = new AnalysisDTO();
			
			saveAnalysisDTO.setUserId(userId); // 요청에서 받은 값
			saveAnalysisDTO.setDocCount(count);
			analysisService.save(saveAnalysisDTO);
			long analysisId=saveAnalysisDTO.getAnalysisId();
			
			JsonNode resultsNode = root.path("results");
			if (!resultsNode.isArray()) {
			    throw new BusinessException(ErrorCode.FASTAPI_ERROR, "AI 응답 results가 배열이 아닙니다.");
			}

			for (JsonNode item : resultsNode) {
			    String imagePath = item.path("image_path").asText(null);
			    String docType = item.path("doc_type").asText(null);
			    String policyVersion = item.path("policy_version").asText(null);
			    long riskScore = item.path("risk_score").asInt();
			    String aiExplanation = item.path("ai_explanation").asText("");

			    
			    if ("CONTRACT".equalsIgnoreCase(docType)) {
			        ContractDTO dto = new ContractDTO();
			        dto.setAnalysisId(analysisId);
			        dto.setImagePath(imagePath);
			        dto.setPolicyVersion(policyVersion);
			        dto.setRiskScore(riskScore);
			        dto.setAiExplanation(aiExplanation);

			        contractService.save(dto);

			    } else if ("REGISTRY".equalsIgnoreCase(docType)) {
			        RegistryDTO dto = new RegistryDTO();
			        dto.setAnalysisId(analysisId);
			        dto.setImagePath(imagePath);
			        dto.setPolicyVersion(policyVersion);
			        dto.setRiskScore(riskScore);
			        dto.setAiExplanation(aiExplanation);

			        registryService.save(dto);
			    }
			}
			ReportDTO saveReportDTO= new ReportDTO(analysisId,totalRiskScore,summary);
			reportService.save(saveReportDTO);
			
			return ResponseEntity.ok(saveReportDTO);
		}
			catch (RestClientException e) {
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
	public ResponseEntity<List<AdminAnalysisViewDTO>> show(@RequestParam("userId") Long userId) {
		return ResponseEntity.ok(analysisService.showByUserId(userId));
	}
	
	@DeleteMapping("/delete/{analysisId}/analysis")
	public ResponseEntity<Void> deleteDocument(@PathVariable("analysisId") Long analysisId) {
		analysisService.deleteAllByAnalysisId(analysisId);
		return ResponseEntity.noContent().build();
	}
	
	
//  @PostMapping("/analyze")
//  public ResponseEntity<String> analyze(@ModelAttribute DocumentsDTO documentsDTO) {
//
//    List<MultipartFile> mfs = documentsDTO.getFileMFs();
//    Long userId = documentsDTO.getUserId();
//
//    log.info("문서 분석 요청 수신 userId={}", userId);
//
//    if (mfs == null || mfs.isEmpty()) {
//      throw new BusinessException(ErrorCode.FILE_MISSING);
//    }
//    if (userId == null) {
//      throw new BusinessException(ErrorCode.FILE_MISSING, "userId가 없습니다. (요청에 userId가 포함되지 않았습니다)");
//    }
//
//    String response = null;
//
//    try {
//      // 1) 파일 저장
//      String upDir = Tool.getServerDir("documents");
//
//      List<String> filePaths = new ArrayList<>();
//      for (MultipartFile f : mfs) {
//        String savedFilename = Upload.saveFile(f, upDir);
//        filePaths.add(savedFilename);
//      }
//      documentsDTO.setFilePaths(filePaths);
//      // (중요) 파일명/경로를 DB에 남길 필드가 있으면 DTO에 세팅해두기
//      // documentsDTO.setFile1(savedFilename); // 필드명 프로젝트에 맞게
//
//      // 2) FastAPI 호출
//      String url = "http://localhost:8000/document/analyze";
//      log.info("[FASTAPI] 요청 시작 url={}, image_path={}", url);
//
//      HttpHeaders headers = new HttpHeaders();
//      headers.setContentType(MediaType.APPLICATION_JSON);
//
//      Map<String, Object> body = new HashMap<>();
//      body.put("SpringBoot_FastAPI_KEY", llmRequestConfig.getSpringBoot_FastAPI_KEY());
//      body.put("userId", userId);
//      body.put("image_paths", filePaths);
//
//      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//      response = llmRequestConfig.getRestTemplate().postForObject(url, requestEntity, String.class);
//      log.info("[FASTAPI] 응답 수신 (length={})", response == null ? 0 : response);
//
//      // 3) ✅ JSON 파싱 (파싱 실패는 여기서만)
//      ObjectMapper om = new ObjectMapper();
//      JsonNode root;
//      try {
//        root = om.readTree(response);
//      } catch (Exception e) {
//        log.error("❌ JSON 파싱 실패 raw={}", response, e);
//        throw new BusinessException(ErrorCode.FASTAPI_ERROR, "AI 응답 JSON 파싱 실패", e);
//      }
//      JsonNode resultsNode = root.path("results");
//      String docType = decideDocType(resultsNode);
//      
//      DocumentsDTO saveDocumentsDTO = new DocumentsDTO();
//      saveDocumentsDTO.setUserId(userId);          // 요청에서 받은 값
//      saveDocumentsDTO.setDocType(docType);        // CONTRACT/REGISTRY/MIXED/UNKNOWN
//      saveDocumentsDTO.setStatus("UPLOADED");      // 선택(없어도 됨)
//
//      Documents savedDoc = documentsService.save(saveDocumentsDTO);
//      Long docId = savedDoc.getDocId();
//
////      if (!"UNKNOWN".equals(docType)) {
////        // ------------------------
////        // 6) RAG ingest (FastAPI /ingest) -- 추가
////        // ------------------------
////       try {
////         // (1) RAG로 넣을 텍스트 구성
////         String ragText =
////             "[문서 분석 결과]\n" +
////             "- 문서유형: " + docType + "\n" +
////             "- 위험점수: " + riskScore + "\n" +
////             "- 정책버전: " + policyVersion + "\n\n" +
////             "[근거(reasons)]\n" + (reasonsJson != null ? reasonsJson : "(없음)") + "\n\n" +
////             "[추출데이터(parsed)]\n" + (parsedJson != null ? parsedJson : "(없음)") + "\n\n" +
////             "[AI 설명]\n" + (aiExplanation != null ? aiExplanation : "(없음)");
////    
////         // (2) meta 구성 (✅ B안 핵심: user_id, doc_id 반드시 포함)
////         Map<String, Object> meta = new HashMap<>();
////         meta.put("user_id", String.valueOf(userId.longValue()));
////         meta.put("doc_id", String.valueOf(docId));
////         meta.put("doc_type", docType);
////         meta.put("stage", "document"); // 너희가 원하는 이름으로 통일
////    
////         // (3) docs[0] 구성
////         Map<String, Object> oneDoc = new HashMap<>();
////         oneDoc.put("id", "doc:" + docId);   // 문서 식별자(임의)
////         oneDoc.put("text", ragText);
////         oneDoc.put("meta", meta);
////         oneDoc.put("chunk", true);
////         oneDoc.put("chunk_size", 900);
////         oneDoc.put("overlap", 120);
////    
////         Map<String, Object> ingestBody = new HashMap<>();
////         ingestBody.put("docs", List.of(oneDoc));
////    
////         // (4) FastAPI /ingest 호출
////         String ingestUrl = "http://121.160.42.81:8000/ingest";
////    
////         HttpHeaders ingestHeaders = new HttpHeaders();
////         ingestHeaders.setContentType(MediaType.APPLICATION_JSON);
////    
////         HttpEntity<Map<String, Object>> ingestReq = new HttpEntity<>(ingestBody, ingestHeaders);
////    
////         String ingestRes = llmRequestConfig.getRestTemplate()
////             .postForObject(ingestUrl, ingestReq, String.class);
////    
////         log.info("✅ RAG ingest OK docId={}, res={}", docId, ingestRes);
////    
////       } catch (Exception ingestErr) {
////         log.error("❌ RAG ingest 실패 docId={}", docId, ingestErr);
////       }
////      
////      if ("REGISTRY".equals(docType)) {
////        try {
////          // (1) RAG로 넣을 텍스트 구성
////          String ragText = "[문서 분석 결과]\n" + "- 문서유형: " + docType + "\n" + "- 위험점수: " + riskScore + "\n" + "- 정책버전: "
////              + policyVersion + "\n\n" + "[근거(reasons)]\n" + (reasonsJson != null ? reasonsJson : "(없음)") + "\n\n"
////              + "[추출데이터(parsed)]\n" + (parsedJson != null ? parsedJson : "(없음)") + "\n\n" + "[AI 설명]\n"
////              + (aiExplanation != null ? aiExplanation : "(없음)");
////
////          // (2) meta 구성 (✅ B안 핵심: user_id, doc_id 반드시 포함)
////          Map<String, Object> meta = new HashMap<>();
////          meta.put("user_id", String.valueOf(userId.longValue()));
////          meta.put("doc_id", String.valueOf(docId));
////          meta.put("doc_type", docType);
////          meta.put("stage", "document"); // 너희가 원하는 이름으로 통일
////
////          // (3) docs[0] 구성
////          Map<String, Object> oneDoc = new HashMap<>();
////          oneDoc.put("id", "doc:" + docId); // 문서 식별자(임의)
////          oneDoc.put("text", ragText);
////          oneDoc.put("meta", meta);
////          oneDoc.put("chunk", true);
////          oneDoc.put("chunk_size", 900);
////          oneDoc.put("overlap", 120);
////
////          Map<String, Object> ingestBody = new HashMap<>();
////          ingestBody.put("docs", List.of(oneDoc));
////
////          // (4) FastAPI /ingest 호출
////          String ingestUrl = "http://localhost:8000/ingest";
////
////          HttpHeaders ingestHeaders = new HttpHeaders();
////          ingestHeaders.setContentType(MediaType.APPLICATION_JSON);
////
////          HttpEntity<Map<String, Object>> ingestReq = new HttpEntity<>(ingestBody, ingestHeaders);
////
////          String ingestRes = llmRequestConfig.getRestTemplate().postForObject(ingestUrl, ingestReq, String.class);
////
////          log.info("✅ RAG ingest OK docId={}, res={}", docId, ingestRes);
////
////        } catch (Exception ingestErr) {
////          log.error("❌ RAG ingest 실패 docId={}", docId, ingestErr);
////        }
////      }
////        if ("REGISTRY".equals(docType)) {
////          try {
////            documentReportService.save(reportDTO);
////            log.info("✅ DocumentReport 저장 OK => docId={}", docId);
////          } catch (Exception e) {
////            log.error("❌ DocumentReport 저장 실패(파싱은 성공) docId={}", docId, e);
////            throw e;
////          }
////        } else {
////          log.info("ℹ️ Report 저장 스킵 docType={}", docType);
////        }
////        String fileNameOnly = Paths.get(savedFilename).getFileName().toString();
////        ((com.fasterxml.jackson.databind.node.ObjectNode) root).put("image_path", "/files/" + fileNameOnly);
//      String finalResponse = om.writeValueAsString(root);
////        log.info(finalResponse);
////        return ResponseEntity.ok(finalResponse);
////      // ✅ 응답 JSON에 "Spring DB docId"를 반드시 포함시킨다 (프론트가 이걸 저장해야 함)
////      if (root.isObject()) {
////        com.fasterxml.jackson.databind.node.ObjectNode obj = (com.fasterxml.jackson.databind.node.ObjectNode) root;
////
////        // FastAPI가 내려준 tmp/anonymous는 혼란이므로 Spring 값으로 덮어쓰기
////        obj.put("docId", docId);                          // ✅ 프론트용(카멜 케이스)
////        obj.put("userId", userId.longValue());            // ✅ 프론트용
////        obj.put("docType", docType);                      // ✅ 프론트용
////
////        // FastAPI 내부키도 같이 덮어주면(선택) 디버깅이 쉬움
////        obj.put("doc_id", String.valueOf(docId));         // ✅ snake_case도 docId로 통일
////        obj.put("user_id", String.valueOf(userId.longValue()));
////        obj.put("doc_type", docType);
////        obj.put("stage", "document");                     // ingest meta와 통일(선택)
////      }
////      
////      String fileNameOnly = Paths.get(savedFilename).getFileName().toString();
////      ((com.fasterxml.jackson.databind.node.ObjectNode) root)
////      .put("image_path", "/files/" + fileNameOnly);
////      String finalResponse = om.writeValueAsString(root);
////      log.info(finalResponse);
//      return ResponseEntity.ok(finalResponse);
//    } catch (RestClientException e) {
//      throw new BusinessException(ErrorCode.FASTAPI_ERROR, "FastAPI 호출 실패: " + e.getMessage(), e);
//    } catch (BusinessException e) {
//      throw e;
//    } catch (Exception e) {
//      log.error("❌ 처리 중 예외 발생 raw={}", response, e);
//      throw new BusinessException(ErrorCode.FASTAPI_ERROR, "문서 분석 처리 중 오류", e);
//    } finally {
//      MDC.clear();
//    }
//  }

//	@GetMapping("/show")
//	public ResponseEntity<List<AdminDocumentViewDTO>> show(@RequestParam("userId") Long userId) {
//		return ResponseEntity.ok(documentsService.showByUserId(userId));
//	}

//	@DeleteMapping("/delete/{docId}/report")
//	public ResponseEntity<Void> deleteReport(@PathVariable("docId") Long docId) {
//		documentsService.deleteReportByDocId(docId);
//		return ResponseEntity.noContent().build();
//	}

//	@DeleteMapping("/delete/{docId}/document")
//	public ResponseEntity<Void> deleteDocument(@PathVariable("docId") Long docId) {
//		documentsService.deleteDocumentByDocId(docId);
//		log.info("docId", docId);
//		return ResponseEntity.noContent().build();
//	}

	private Integer toInt(Object v) {
		if (v == null)
			return null;
		if (v instanceof Number n)
			return n.intValue();
		try {
			return Integer.parseInt(String.valueOf(v));
		} catch (Exception e) {
			return null;
		}
	}

}
