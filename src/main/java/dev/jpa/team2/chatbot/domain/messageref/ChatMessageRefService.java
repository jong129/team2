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

// 1. 회원 조회는 내 세션 소유권 확인하고 근거 반환
// 2. 관리자는 근거/통계를 바로 반환
// 3. 통계는 DB결과 타입이 환경마다 다르게 오는 이슈를 방어하는 파싱 유틸이 있음.

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageRefService {

    private final ChatMessageRefRepository refRepo;
    private final ChatMessageRepository messageRepo;
    private final ChatSessionService sessionService;

    // ===== 회원 : 내 chatId 근거 조회 =====
    public ChatMessageRefDto getRefsForMyChat(Long memberId, Long chatId) {
        // 메시지 존재 확인
        ChatMessage msg = messageRepo.findById(chatId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 chatId=" + chatId));
        
        // 내가 소유한 세션의 메시지인지 권한 체크
        sessionService.requireOwnedSession(memberId, msg.getSessionId());

        List<ChatMessageRefDto.RefItem> refs =
            refRepo.findByChatIdOrderByRankNoAsc(chatId)  // 근거 조회
                   .stream()
                   .map(ChatMessageRefDto.RefItem::from)  // RefItem.from()로 변환
                   .toList();

        log.info("[ChatMessageRefService] getRefsForMyChat | memberId={} chatId={} refs={}",
                memberId, chatId, refs.size());
        
        return ChatMessageRefDto.builder()  // DTO 반환 : mode = CHAT_REFS, scope = MY
            .mode("CHAT_REFS")
            .scope("MY")
            .chatId(chatId)
            .refCount(refs.size())
            .refs(refs)
            .build();
    }
    
    // ====== 관리자 : chatId 근거 조회 =====
    public ChatMessageRefDto getRefsForAdmin(Long chatId) {
        // 권한 체크 없이 근거 조회
        List<ChatMessageRefDto.RefItem> refs =
            refRepo.findByChatIdOrderByRankNoAsc(chatId)
                   .stream()
                   .map(ChatMessageRefDto.RefItem::from)
                   .toList();

        log.warn("[ChatMessageRefService] getRefsForAdmin | chatId={} refs={}", chatId, refs.size());

        return ChatMessageRefDto.builder()  // 반환 : scope = ALL
            .mode("CHAT_REFS")
            .scope("ALL")
            .chatId(chatId)
            .refCount(refs.size())
            .refs(refs)
            .build();
    }
    
    // ===== 전체 통계 =====
    public ChatMessageRefDto statsAll(int days, int top, int badN) {
        // 입력값 안전화
        int safeDays = Math.max(1, Math.min(days, 365));
        int safeTop  = Math.max(1, Math.min(top, 50));
        int safeBadN = Math.max(1, Math.min(badN, 50));
        
        // since 계산
        LocalDateTime since = LocalDateTime.now().minusDays(safeDays);

        // statsAll 결과가 환경에 따라 [[count, avg]]처럼 한 겹 더 감싸져 올 수 있어서 unwrapRowDeep로 안전하게 풀어 읽음
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
        
        // 각 row를 TopChunkStat로 매핑
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
        
        return ChatMessageRefDto.builder()  // 반환 : mode = STATS, scope = ALL
            .mode("STATS")
            .scope("ALL")
            .days(safeDays)
            .badDislikeN(safeBadN)
            .totalRefs(totalRefs)
            .avgScore(avgScore)
            .topChunks(topChunks)
            .build();
    }

    // ===== 문제 chunk 후보 ======
    public ChatMessageRefDto badChunksAll(int days, int top, int badN) {
        int safeDays = Math.max(1, Math.min(days, 365));
        int safeTop  = Math.max(1, Math.min(top, 50));
        int safeBadN = Math.max(1, Math.min(badN, 50));
        
        // since 계산
        LocalDateTime since = LocalDateTime.now().minusDays(safeDays);
        
        // 나쁜 답변만 대상으로 후보 뽑음
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

        return ChatMessageRefDto.builder()  // 반환 : mode = BAD_CHUNKS
            .mode("BAD_CHUNKS")
            .scope("ALL")
            .days(safeDays)
            .badDislikeN(safeBadN)
            .badChunks(badChunks)
            .build();
    }


    // ===== 내부 유틸 (안전 파싱) =====
    /** row가 Object[]가 아닐 수도 있어서 방어 */
    private static Object[] asRow(Object row) {
        if (row == null) return new Object[0];
        if (row instanceof Object[] arr) return arr;
        return new Object[]{ row };
    }

    /** statsAll/statsMy처럼 "단일 row" 결과가 [count, avg], [[count, avg]]  (한 겹 더 감싸짐)으로 둘 다 올 수 있어서 깊게 1회 unwrap */
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
    
    /** null/Number/중첩배열까지 방어해서 안전 변환 */
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
