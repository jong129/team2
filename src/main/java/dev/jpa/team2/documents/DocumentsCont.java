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

//RestTemplate
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
//Multipart/form-data
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

//Java
import java.util.Map;
import java.util.HashMap;

//예외 처리
import java.io.IOException;

import dev.jpa.team2.tool.LLMRequestConfig;
import dev.jpa.team2.tool.Tool;
import dev.jpa.team2.tool.Upload;


@RestController
@RequestMapping("/documents")
public class DocumentsCont {
  @Autowired
  DocumentsService documentsService;
  @Autowired
  private LLMRequestConfig llmRequestConfig;
  @PostMapping("/analyze")
  public ResponseEntity<String> analyze(
          @ModelAttribute DocumentsDTO documentsDTO
  ) {

      MultipartFile mf = documentsDTO.getFile1MF();

      if (mf == null || mf.isEmpty()) {
          return ResponseEntity.badRequest().body("파일이 없습니다.");
      }

      // -------------------------------------------------
      // 1. 서버에 파일 저장
      // -------------------------------------------------
      String upDir = Tool.getServerDir("documents");
      String savedFilename = Upload.saveFile(mf, upDir);


      // -------------------------------------------------
      // 2. FastAPI로 JSON 전송
      // -------------------------------------------------
      String url = "http://localhost:8000/document/analyze";

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON); 

      // 요청 바디에 담을 데이터
      System.out.println("-> SpringBoot_FastAPI_KEY: " + this.llmRequestConfig.getSpringBoot_FastAPI_KEY());
      
      Map<String, Object> body = new HashMap<>();
      body.put("SpringBoot_FastAPI_KEY", this.llmRequestConfig.getSpringBoot_FastAPI_KEY());
      body.put("image_path", savedFilename);
      
      // HttpEntity로 헤더 + 바디 묶기
      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
      
      // POST 요청 보내고, 결과를 String으로 받기
      String response = this.llmRequestConfig.getRestTemplate().postForObject(url, requestEntity, String.class);
      System.out.println("-> response: " + response);
      

      return ResponseEntity.ok(response);
  }


}
