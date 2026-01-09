package dev.jpa.team2.chatbot.api.stats;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.domain.feedback.ChatMessageFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 피드백(좋아요/싫어요) 기반 품질 통계를 만드는 서비스

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션
public class ChatFeedbackStatsService {

    private final ChatMessageFeedbackRepository repo; // feedback 테이블 기준

    public ChatFeedbackStatsDto statsAll(int days, int top) { // 입력값 안전화
        int safeDays = Math.max(1, Math.min(days, 365));  // 너무 큰 기간으로 DB 과부하 방지
        int safeTop  = Math.max(1, Math.min(top, 50));  // top N 리스트 과도한 응답 방지

        LocalDateTime since = LocalDateTime.now().minusDays(safeDays);  // 기간 기준 시점 계산

        // 전체 totals 집계(좋아요/싫어요)
        Object[] totalsRow = repo.totalsAll(since).stream() // totalsAll : List<Object[]>로 받고 첫 행 꺼내기
            .findFirst()
            .orElse(new Object[]{0, 0});  // 결과가 비면 {0,0}으로 대체해서 null-safe

        long likes    = totalsRow[0] == null ? 0L : ((Number) totalsRow[0]).longValue();
        long dislikes = totalsRow[1] == null ? 0L : ((Number) totalsRow[1]).longValue();
        long net      = likes - dislikes;
        
        // 모델별 통계
        List<ChatFeedbackStatsDto.ModelStat> byModel = repo.byModelAll(since).stream()
            .map(r -> {
                String model = String.valueOf(r[0]);
                long l = r[1] == null ? 0L : ((Number) r[1]).longValue();
                long d = r[2] == null ? 0L : ((Number) r[2]).longValue();
                return ChatFeedbackStatsDto.ModelStat.builder()   // 이걸 DTO의 ModelStat으로 변환해서 모델별 품질 비교표를 만듬
                    .model(model)
                    .likes(l)
                    .dislikes(d)
                    .net(l - d)
                    .build();
            })
            .toList();
        
        // 싫어요 많은 메시지 TOP N
        List<ChatFeedbackStatsDto.TopMessage> topDisliked = repo
            .topDislikedAll(since, PageRequest.of(0, safeTop)).stream()   // PageRequest.of(0, safeTop)으로 상위 safeTop개만 가져옴 (성능/응답 크기 제어)
            .map(r -> {
                Long chatId = r[0] == null ? null : ((Number) r[0]).longValue();
                String model = String.valueOf(r[1]);
                long d = r[2] == null ? 0L : ((Number) r[2]).longValue();
                long l = r[3] == null ? 0L : ((Number) r[3]).longValue();
                return ChatFeedbackStatsDto.TopMessage.builder()
                    .chatId(chatId)
                    .model(model)
                    .dislikes(d)
                    .likes(l)
                    .net(l - d)
                    .build();
            })
            .toList();

        log.info("[FeedbackStats] statsAll | days={} likes={} dislikes={}", safeDays, likes, dislikes);
        
        // 최종 DTO 조립 후 반환
        return ChatFeedbackStatsDto.builder() 
            .scope("ALL")
            .days(safeDays)
            .totalLikes(likes)
            .totalDislikes(dislikes)
            .net(net)
            .byModel(byModel)
            .topDislikedMessages(topDisliked)
            .build();
    }
}
