package dev.jpa.team2.chatbot.messageref;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.message.ChatMessage;
import dev.jpa.team2.chatbot.message.ChatMessageRepository;
import dev.jpa.team2.chatbot.session.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageRefService {

    private final ChatMessageRefRepository refRepo;
    private final ChatMessageRepository messageRepo;
    private final ChatSessionService sessionService;

    // 회원: 내 AI 답변(chatId)의 근거 조회
    public ChatMessageRefDto getRefsForMyChat(Long memberId, Long chatId) {

        ChatMessage msg = messageRepo.findById(chatId)
            .orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 chatId=" + chatId));

        // 내 세션인지 검증
        sessionService.requireOwnedSession(memberId, msg.getSessionId());

        List<ChatMessageRefDto.RefItem> refs =
            refRepo.findByChatIdOrderByCreatedAtAsc(chatId)
                   .stream()
                   .map(ChatMessageRefDto.RefItem::from)
                   .toList();

        log.info("[ChatMessageRefService] getRefsForMyChat | memberId={} chatId={} refs={}",
                memberId, chatId, refs.size());

        return ChatMessageRefDto.builder()
            .mode("CHAT_REFS")
            .scope("MY")
            .chatId(chatId)
            .refCount(refs.size())
            .refs(refs)
            .build();
    }

    // 관리자/디버그: chatId 기준 근거 조회
    public ChatMessageRefDto getRefsForAdmin(Long chatId) {

        List<ChatMessageRefDto.RefItem> refs =
            refRepo.findByChatIdOrderByCreatedAtAsc(chatId)
                   .stream()
                   .map(ChatMessageRefDto.RefItem::from)
                   .toList();

        log.warn("[ChatMessageRefService] getRefsForAdmin | chatId={} refs={}",
                chatId, refs.size());

        return ChatMessageRefDto.builder()
            .mode("CHAT_REFS")
            .scope("ALL")
            .chatId(chatId)
            .refCount(refs.size())
            .refs(refs)
            .build();
    }

    // 품질 분석: 회원별
    public ChatMessageRefDto statsMy(Long memberId, int days, int top) {
        return statsInternal("MY", memberId, days, top);
    }

    // 품질 분석: 전체
    public ChatMessageRefDto statsAll(int days, int top) {
        return statsInternal("ALL", null, days, top);
    }

    // 내부 공통 로직
    private ChatMessageRefDto statsInternal(
        String scope,
        Long memberId,
        int days,
        int top
    ) {
        int safeDays = Math.max(1, Math.min(days, 365));
        int safeTop  = Math.max(1, Math.min(top, 50));

        LocalDateTime since = LocalDateTime.now().minusDays(safeDays);

        Object[] stats;
        List<Object[]> rows;

        if ("MY".equals(scope)) {
            stats = refRepo.statsMy(memberId, since);
            rows  = refRepo.topChunksMy(
                        memberId, since, PageRequest.of(0, safeTop));
        } else {
            stats = refRepo.statsAll(since);
            rows  = refRepo.topChunksAll(
                        since, PageRequest.of(0, safeTop));
        }

        long totalRefs =
            stats[0] == null ? 0L : ((Number) stats[0]).longValue();

        Double avgScore =
            stats[1] == null ? null : ((Number) stats[1]).doubleValue();

        List<ChatMessageRefDto.TopChunkStat> topChunks =
            rows.stream()
                .map(r -> ChatMessageRefDto.TopChunkStat.builder()
                    .chunkId(((Number) r[0]).longValue())
                    .count(((Number) r[1]).longValue())
                    .avgScore(
                        r[2] == null ? null : ((Number) r[2]).doubleValue()
                    )
                    .build()
                )
                .toList();

        log.info("[ChatMessageRefService] stats | scope={} memberId={} days={} totalRefs={} avgScore={}",
                scope, memberId, safeDays, totalRefs, avgScore);

        return ChatMessageRefDto.builder()
            .mode("STATS")
            .scope(scope)
            .days(safeDays)
            .totalRefs(totalRefs)
            .avgScore(avgScore)
            .topChunks(topChunks)
            .build();
    }
}
