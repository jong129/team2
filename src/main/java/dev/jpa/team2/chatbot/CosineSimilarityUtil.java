package dev.jpa.team2.chatbot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 임베딩 벡터(숫자 배열)를 다루기 위한 유틸
// 문자열로 저장된 벡터를 List<Double>로 변환하고 두 벡터 간의 코사인 유사도를 계산

public class CosineSimilarityUtil {
  
    private CosineSimilarityUtil() {} // util class
    
    // DB나 로그에 문자열 형태로 저장된 벡터를 실제 수치 계산이 가능한 List<Double>로 변환
    // "[0.1, 0.2, -0.3]" → List<Double> 
    public static List<Double> parseVector(String vectorString) {
      if (vectorString == null) return Collections.emptyList(); //  null 체크
      
      String cleaned = vectorString.trim();
      if (cleaned.isEmpty()) return Collections.emptyList();
      
      // 대괄호 제거(양끝에만 있는 경우도 많아서 안전하게 양끝만 제거)
      if (cleaned.startsWith("[")) cleaned = cleaned.substring(1);
      if (cleaned.endsWith("]")) cleaned = cleaned.substring(0, cleaned.length() - 1);
      
      cleaned = cleaned.trim();
      if (cleaned.isEmpty()) return Collections.emptyList();  // 빈 문자열 체크
      
      String[] tokens = cleaned.split(","); // 콤마 기준 분리
    
      List<Double> vector = new ArrayList<>(tokens.length);
      for (String token : tokens) {
        if (token == null) continue;
        String t = token.trim();
        if (t.isEmpty()) continue;

        try {
          double v = Double.parseDouble(t);    
          // NaN/Infinity 같은 비정상 값 방어
          if (!Double.isFinite(v)) continue;
            vector.add(v);
        } catch (NumberFormatException ignore) {
          // 숫자 아닌 토큰은 무시 (필요하면 throw로 변경)
        }
      }
      return vector.isEmpty() ? Collections.emptyList() : vector;
    }


    // cosine similarity 계산 : 두 벡터가 얼마나 뱡향이 비슷한지를 수치로 계산 -> 의미 유사도 측정(RAG, 문서검색, 챗봇근거선택)
    public static double cosineSimilarity(List<Double> v1, List<Double> v2) {
      if (v1 == null || v2 == null) return 0.0;
      
      int n1 = v1.size();
      int n2 = v2.size();
      if (n1 == 0 || n2 == 0) return 0.0;
      
      // 벡터 크기 검증 : 임베딩 차원이 다르면 계산 불가
      if (n1 != n2) {
        throw new IllegalArgumentException("Vector size mismatch: v1=" + n1 + ", v2=" + n2);
      }

      double dot = 0.0; // 내적 (벡터 간 겹침 정도)
      double normA = 0.0; // 각 벡터의 크기(길이)
      double normB = 0.0; 
      
      // 반복 계산
      for (int i = 0; i < n1; i++) {
        Double aObj = v1.get(i);
        Double bObj = v2.get(i);

        // null 값이 들어오는 경우 방어(데이터 품질 문제 대비)
        double a = (aObj == null) ? 0.0 : aObj;
        double b = (bObj == null) ? 0.0 : bObj;

        // NaN/Infinity 방어
        if (!Double.isFinite(a)) a = 0.0;
        if (!Double.isFinite(b)) b = 0.0;

        dot += a * b;
        normA += a * a; 
        normB += b * b;
      }
      
      // 0 벡터 방어
      if (normA == 0.0 || normB == 0.0) return 0.0;

      double denom = Math.sqrt(normA) * Math.sqrt(normB);
      if (denom == 0.0 || !Double.isFinite(denom)) return 0.0;

      double sim = dot / denom;

      // NaN/Infinity 방어
      if (!Double.isFinite(sim)) return 0.0;

      // 부동소수점 오차로 1.0000000002 같은 값이 나올 수 있어 클램프
      if (sim > 1.0) return 1.0;
      if (sim < -1.0) return -1.0;
      return sim;
    }
}
