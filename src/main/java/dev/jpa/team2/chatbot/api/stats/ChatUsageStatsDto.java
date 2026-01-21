package dev.jpa.team2.chatbot.api.stats;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

// 챗봇 사용량 / 성능 통계 DTO
// 특정 기간(days) 동안, 챗봇이 얼마나 많이 쓰였고(요청 수), 얼마나 비용/성능이 들었는지(토큰/지연)를 요약한 통계 응답

@Getter
@Setter
public class ChatUsageStatsDto {

    private int days; // 통계 기준 기간

    private long totalRequests;   // ROLE=ASSISTANT 응답 수(AI가 실제로 응답한 횟수)
    private long totalTokens;     // 합계 토큰
    private Double avgTokens;     // 요청당 평균 토큰
    private Double avgLatencyMs;  // 요청당 평균 지연(ms)

    private List<ByModelRow> byModel = new ArrayList<>(); // 모델별 통계 (모델별 사용량/성능 비교용)

    @Getter
    @Setter
    public static class ByModelRow {
        private String model;
        private long requests;  // 몇 번 응답했는지
        private long tokens;  // 토큰을 얼마나 사용했는지
        private Double avgTokens; // 평균적으로 얼마나 무거운 응답인지
        private Double avgLatencyMs;  // 평균 지연이 얼마나 되는지  
    }

    // ------------------------------------------------
    // NativeQuery(Object[]) 변환 유틸 (Oracle 대응) : NativeQuery(Object[]) 결과를 안전하게 long/Double로 변환하는 방어 유틸
    // ------------------------------------------------
    public static long toLong(Object[] arr, int idx) {
        if (arr == null || idx < 0 || idx >= arr.length) return 0L;
        Object v = arr[idx];
        if (v == null) return 0L;
        if (v instanceof BigDecimal bd) return bd.longValue();
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0L; }
    }
    public static Double toDouble(Object[] arr, int idx) {
        if (arr == null || idx < 0 || idx >= arr.length) return null;
        Object v = arr[idx];
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd.doubleValue();
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return null; }
    }
}
