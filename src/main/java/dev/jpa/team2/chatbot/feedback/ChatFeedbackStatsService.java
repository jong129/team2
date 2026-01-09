package dev.jpa.team2.chatbot.feedback;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatFeedbackStatsService {

    private final ChatMessageFeedbackRepository repo;

    public ChatFeedbackStatsDto statsAll(int days, int top) {
        int safeDays = Math.max(1, Math.min(days, 365));
        int safeTop  = Math.max(1, Math.min(top, 50));

        LocalDateTime since = LocalDateTime.now().minusDays(safeDays);

        // ✅ totalsAll: List<Object[]>로 받고 첫 행 꺼내기
        Object[] totalsRow = repo.totalsAll(since).stream()
            .findFirst()
            .orElse(new Object[]{0, 0});

        long likes    = totalsRow[0] == null ? 0L : ((Number) totalsRow[0]).longValue();
        long dislikes = totalsRow[1] == null ? 0L : ((Number) totalsRow[1]).longValue();
        long net      = likes - dislikes;

        List<ChatFeedbackStatsDto.ModelStat> byModel = repo.byModelAll(since).stream()
            .map(r -> {
                String model = String.valueOf(r[0]);
                long l = r[1] == null ? 0L : ((Number) r[1]).longValue();
                long d = r[2] == null ? 0L : ((Number) r[2]).longValue();
                return ChatFeedbackStatsDto.ModelStat.builder()
                    .model(model)
                    .likes(l)
                    .dislikes(d)
                    .net(l - d)
                    .build();
            })
            .toList();

        List<ChatFeedbackStatsDto.TopMessage> topDisliked = repo
            .topDislikedAll(since, PageRequest.of(0, safeTop)).stream()
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
