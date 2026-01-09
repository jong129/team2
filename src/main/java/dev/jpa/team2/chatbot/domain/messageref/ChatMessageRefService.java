package dev.jpa.team2.chatbot.domain.messageref;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.domain.message.ChatMessage;
import dev.jpa.team2.chatbot.domain.message.ChatMessageRepository;
import dev.jpa.team2.chatbot.domain.session.ChatSessionService;
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

    // -----------------------------
    // 근거 조회 (회원/관리자)
    // -----------------------------

    public ChatMessageRefDto getRefsForMyChat(Long memberId, Long chatId) {

        ChatMessage msg = messageRepo.findById(chatId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 chatId=" + chatId));

        sessionService.requireOwnedSession(memberId, msg.getSessionId());

        List<ChatMessageRefDto.RefItem> refs =
            refRepo.findByChatIdOrderByRankNoAsc(chatId)
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

    public ChatMessageRefDto getRefsForAdmin(Long chatId) {

        List<ChatMessageRefDto.RefItem> refs =
            refRepo.findByChatIdOrderByRankNoAsc(chatId)
                   .stream()
                   .map(ChatMessageRefDto.RefItem::from)
                   .toList();

        log.warn("[ChatMessageRefService] getRefsForAdmin | chatId={} refs={}", chatId, refs.size());

        return ChatMessageRefDto.builder()
            .mode("CHAT_REFS")
            .scope("ALL")
            .chatId(chatId)
            .refCount(refs.size())
            .refs(refs)
            .build();
    }

    // -----------------------------
    // ✅ 품질 통계(ALL)
    // -----------------------------

    public ChatMessageRefDto statsAll(int days, int top, int badN) {
        int safeDays = Math.max(1, Math.min(days, 365));
        int safeTop  = Math.max(1, Math.min(top, 50));
        int safeBadN = Math.max(1, Math.min(badN, 50));

        LocalDateTime since = LocalDateTime.now().minusDays(safeDays);

        // ✅ statsAll 결과가 환경에 따라 [[count, avg]]처럼 한 겹 더 감싸져 올 수 있어서 "깊게" 풀어 읽는다.
        Object rawStats = refRepo.statsAll(since);      // 컴파일 타입은 Object[]지만 런타임 구조 방어용
        Object[] stats = unwrapRowDeep(rawStats);

        List<Object[]> rows = refRepo.topChunksAll(since, safeBadN, PageRequest.of(0, safeTop));

        long totalRefs = nzLong(stats, 0, 0L);
        double avgScore = nzDouble(stats, 1, 0.0);

        // (디버그 필요하면 잠깐 켜서 확인)
        log.debug("[ChatMessageRefService] statsAll rawStatsClass={} statsLen={} stats0={} stats1={}",
                rawStats == null ? "null" : rawStats.getClass().getName(),
                stats.length,
                stats.length > 0 ? stats[0] : null,
                stats.length > 1 ? stats[1] : null
        );

        List<ChatMessageRefDto.TopChunkStat> topChunks =
            rows.stream()
                .map(ChatMessageRefService::asRow)
                .map(r -> ChatMessageRefDto.TopChunkStat.builder()
                    .chunkId(nzLong(r, 0, 0L))
                    .count(nzLong(r, 1, 0L))
                    .avgScore(nzDouble(r, 2, 0.0))
                    .sumDislikes(nzLong(r, 3, 0L))
                    .badAnswerCount(nzLong(r, 4, 0L))
                    .build()
                )
                .toList();

        log.info("[ChatMessageRefService] statsAll | days={} badN={} totalRefs={} avgScore={} topChunks={}",
                safeDays, safeBadN, totalRefs, avgScore, topChunks.size());

        return ChatMessageRefDto.builder()
            .mode("STATS")
            .scope("ALL")
            .days(safeDays)
            .badDislikeN(safeBadN)
            .totalRefs(totalRefs)
            .avgScore(avgScore)
            .topChunks(topChunks)
            .build();
    }

    // -----------------------------
    // ✅ bad-chunks 후보(ALL)
    // -----------------------------

    public ChatMessageRefDto badChunksAll(int days, int top, int badN) {
        int safeDays = Math.max(1, Math.min(days, 365));
        int safeTop  = Math.max(1, Math.min(top, 50));
        int safeBadN = Math.max(1, Math.min(badN, 50));

        LocalDateTime since = LocalDateTime.now().minusDays(safeDays);

        List<Object[]> rows = refRepo.badChunksAll(since, safeBadN, PageRequest.of(0, safeTop));

        List<ChatMessageRefDto.TopChunkStat> badChunks =
            rows.stream()
                .map(ChatMessageRefService::asRow)
                .map(r -> ChatMessageRefDto.TopChunkStat.builder()
                    .chunkId(nzLong(r, 0, 0L))
                    .count(nzLong(r, 1, 0L))
                    .avgScore(nzDouble(r, 2, 0.0))
                    .sumDislikes(nzLong(r, 3, 0L))
                    .badAnswerCount(nzLong(r, 4, 0L))
                    .build()
                )
                .toList();

        log.info("[ChatMessageRefService] badChunksAll | days={} badN={} candidates={}",
                safeDays, safeBadN, badChunks.size());

        return ChatMessageRefDto.builder()
            .mode("BAD_CHUNKS")
            .scope("ALL")
            .days(safeDays)
            .badDislikeN(safeBadN)
            .badChunks(badChunks)
            .build();
    }

    // -----------------------------
    // 내부 유틸 (안전 파싱)
    // -----------------------------

    /** rows(List<Object[]>)용: row가 단일 값으로 올 수도 있어 방어 */
    private static Object[] asRow(Object row) {
        if (row == null) return new Object[0];
        if (row instanceof Object[] arr) return arr;
        return new Object[]{ row };
    }

    /**
     * ✅ statsAll/statsMy처럼 "단일 row" 결과가
     * - [count, avg]
     * - [[count, avg]]  (한 겹 더 감싸짐)
     * 둘 다 올 수 있어서 깊게 1회 unwrap
     */
    private static Object[] unwrapRowDeep(Object row) {
        if (row == null) return new Object[0];

        if (row instanceof Object[] arr) {
            // [[count, avg]] 같은 케이스
            if (arr.length == 1 && arr[0] instanceof Object[] inner) {
                return inner;
            }
            return arr;
        }

        return new Object[]{ row };
    }

    private static long nzLong(Object[] arr, int idx, long def) {
        if (arr == null || arr.length <= idx || arr[idx] == null) return def;
        Object v = arr[idx];

        if (v instanceof Number n) return n.longValue();

        // 혹시 중첩 배열이면 내부 숫자를 찾아봄
        if (v instanceof Object[] nested) {
            for (Object x : nested) {
                if (x instanceof Number n2) return n2.longValue();
            }
        }
        return def;
    }

    private static double nzDouble(Object[] arr, int idx, double def) {
        if (arr == null || arr.length <= idx || arr[idx] == null) return def;
        Object v = arr[idx];

        if (v instanceof Number n) return n.doubleValue();

        if (v instanceof Object[] nested) {
            for (Object x : nested) {
                if (x instanceof Number n2) return n2.doubleValue();
            }
        }
        return def;
    }
}
