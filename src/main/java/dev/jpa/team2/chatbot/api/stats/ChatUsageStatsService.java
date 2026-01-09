package dev.jpa.team2.chatbot.api.stats;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.domain.message.ChatMessageRepository;
import lombok.RequiredArgsConstructor;

// 토큰/지연시간 기반 사용량/성능 통계를 만드는 서비스

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatUsageStatsService {

    private final ChatMessageRepository messageRepo;  // message 테이블의 usage 컬럼 기반: model, tokens, latency
    
    public ChatUsageStatsDto loadUsageStats(int days) {

        // 모델별  사용량/성능(byModel) 집계
        List<Object[]> rows = messageRepo.usageByModel(days);
        List<ChatUsageStatsDto.ByModelRow> byModel = new ArrayList<>();
        
        for (Object[] r : rows) {   // toLong, toDouble은 Oracle/native query에서 BigDecimal로 오는 값도 안전하게 처리하려고 만든 유틸
            ChatUsageStatsDto.ByModelRow bm = new ChatUsageStatsDto.ByModelRow();
            bm.setModel(r[0] == null ? "UNKNOWN" : String.valueOf(r[0]));
            bm.setRequests(ChatUsageStatsDto.toLong(r, 1));
            bm.setTokens(ChatUsageStatsDto.toLong(r, 2));
            bm.setAvgTokens(ChatUsageStatsDto.toDouble(r, 3));
            bm.setAvgLatencyMs(ChatUsageStatsDto.toDouble(r, 4));
            byModel.add(bm);
        }

        // 전체 summary 집계 (전체 요청/ 토큰/ 평균)
        List<Object[]> sumRows = messageRepo.usageSummary(days);
        Object[] sum = (sumRows == null || sumRows.isEmpty())
            ? new Object[]{0, 0, null, null}
            : sumRows.get(0);

        long totalRequests = ChatUsageStatsDto.toLong(sum, 0);
        long totalTokens   = ChatUsageStatsDto.toLong(sum, 1);
        Double avgTokens   = ChatUsageStatsDto.toDouble(sum, 2);
        Double avgLatencyMs= ChatUsageStatsDto.toDouble(sum, 3);

        // fallback 처리 (요약이 0인데 모델별은 있으면 환산). 단, avgTokens/avgLatency는 단순 평균이 위험해서 여기서는 그대로 null/기존값 유지
        if (totalRequests == 0 && !byModel.isEmpty()) {
            totalRequests = byModel.stream().mapToLong(ChatUsageStatsDto.ByModelRow::getRequests).sum();
            totalTokens   = byModel.stream().mapToLong(ChatUsageStatsDto.ByModelRow::getTokens).sum();
        }

        // 최종 DTO 조립 후 반환
        ChatUsageStatsDto dto = new ChatUsageStatsDto();
        dto.setDays(days);
        dto.setTotalRequests(totalRequests);
        dto.setTotalTokens(totalTokens);
        dto.setAvgTokens(avgTokens);
        dto.setAvgLatencyMs(avgLatencyMs);
        dto.setByModel(byModel);
        return dto;
    }
}
