package dev.jpa.team2.chatbot;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.jpa.team2.chatbot.domain.embeddingchunk.EmbeddingChunkDto;
import dev.jpa.team2.chatbot.domain.embeddingchunk.EmbeddingChunkRepository;
import lombok.RequiredArgsConstructor;

// 사용자 질문 임베딩(queryVector)과 DB에 저장된 모든 문서 임베딩(EmbeddingChunk)을 비교해서 코사인 유사도가 높은 상위 K개 chunk를 찾아주는 서비스
// user question -> List<Double> queryVector -> EmbeddingSimilarityService -> Top K EmbeddingChunk -> RAG Context 구성 -> LLM 답변 생성

@Service
@RequiredArgsConstructor
public class EmbeddingSimilarityService {

    private final EmbeddingChunkRepository embeddingChunkRepository;  // DB에 저장된 문서 임베딩 chunk 테이블
    
    // 유사도(score)가 포함된 chunk 리스트
    public List<EmbeddingChunkDto.SearchResult> searchTopK(List<Double> queryVector, int topK) {
      // topK 안전 처리 (topK <= 0 방지) : 너무 큰 값으로 인한 성능 저하 방지
      int safeTopK = Math.max(1, Math.min(topK, 50));
      
      // DB 전체 chunk 로딩
      return embeddingChunkRepository.findAll().stream()
          // Stream 기반 유사도 계산
          .map(chunk -> {
              List<Double> chunkVector = CosineSimilarityUtil.parseVector(chunk.getVectorData()); // DB에 문자열로 저장된 벡터 파싱
              double score = CosineSimilarityUtil.cosineSimilarity(queryVector, chunkVector); // 질문 벡터 vs 문서 벡터 코사인 유사도 계산
              return EmbeddingChunkDto.SearchResult.of(chunk, score); // 결과 DTO로 변환
          })
          .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore())) // 유사도 내림차순 정렬
          .limit(safeTopK)  // 상위 K개만 추출
          .toList();  // List로 변환
    }
}
