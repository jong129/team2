package dev.jpa.team2.chatbot.domain.embeddingchunk;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jpa.team2.chatbot.FastApiLlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 요청 텍스트를 청크로 쪼개고, 각 청크 임베딩을 만들고, DB에 넣는 역활

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmbeddingChunkService {

    private final EmbeddingChunkRepository embeddingChunkRepository;
    private final FastApiLlmService llmService;
    private final ObjectMapper objectMapper;
    
    // 임베딩 생성
    public EmbeddingChunkDto.CreateResponse create(EmbeddingChunkDto.CreateRequest req) {
        // 요청 검증
        if (req == null || req.getFileId() == null || req.getChunkText() == null) {
          throw new IllegalArgumentException("fileId/chunkText는 필수입니다.");
        }
      
        // 핵심 처리
        int inserted = saveChunksFromText(req.getFileId(), req.getChunkText());
      
        // 응답
        return EmbeddingChunkDto.CreateResponse.builder()
          .success(true)
          .fileId(req.getFileId())
          .inserted(inserted)
          .build();

    }
    
    // 텍스트를 chunk로 쪼개서 embedding_chunk에 저장
    // fileId 자리에 refId를 넣음
    // @return 저장 성공한 chunk 개수
    public int saveChunksFromText(Long fileId, String text) {
        // 방어 로직
        if (fileId == null) {
            log.warn("[EmbeddingChunkService] fileId is null -> skip");
            return 0;
        }
        if (text == null || text.trim().isEmpty()) {
            log.warn("[EmbeddingChunkService] text empty -> skip | fileId={}", fileId);
            return 0;
        }
        
        // 청크 분할 파라미터 : 텍스트를 800 길이로 자르되, 다음 청크는 80정도 겹치게 해서 문맥이 끊기는 문제를 줄임
        int chunkSize = 800;      // 한 chunk 길이(필요 시 조절)
        int overlap = 80;         // 겹치기(문맥 유지)
        List<String> chunks = splitWithOverlap(text.trim(), chunkSize, overlap);  

        int ok = 0;
        int fail = 0;
        
        // 각 청크 처리 루프
        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i).trim();
            if (chunkText.isEmpty()) continue;

            try {
                // 임베딩 생성
                List<Double> vector = llmService.embedding(chunkText);

                // JSON 문자열로 직렬화
                String vectorJson = objectMapper.writeValueAsString(vector);

                // EmbeddingChunk 엔티티 생성
                EmbeddingChunk e = new EmbeddingChunk();
                e.setFileId(fileId);
                e.setChunkText(chunkText);
                e.setVectorData(vectorJson);
                e.setCreatedAt(LocalDateTime.now());
                
                // 저장
                embeddingChunkRepository.save(e);
                ok++;

            } catch (Exception ex) {
                fail++;
                log.error("[EmbeddingChunkService] save chunk failed | fileId={} idx={} chunkLen={}",
                        fileId, i, chunkText.length(), ex);
                // 실패해도 다음 chunk 계속 진행
            }
        }

        log.info("[EmbeddingChunkService] saveChunksFromText done | fileId={} total={} ok={} fail={}",
                fileId, chunks.size(), ok, fail);

        return ok;
    }

    /** 청크 분할 유틸 */
    private List<String> splitWithOverlap(String text, int chunkSize, int overlap) {
        List<String> out = new ArrayList<>();
        int step = Math.max(1, chunkSize - overlap);  // 최소 1 보장
        
        // start를 step씩 증가시키면서 substring으로 chunk 생성
        for (int start = 0; start < text.length(); start += step) {
            int end = Math.min(text.length(), start + chunkSize);
            out.add(text.substring(start, end));
            if (end == text.length()) break;
        }
        return out;
    }
}
