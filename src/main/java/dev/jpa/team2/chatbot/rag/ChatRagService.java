package dev.jpa.team2.chatbot.rag;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.EmbeddingSimilarityService;
import dev.jpa.team2.chatbot.FastApiLlmService;
import dev.jpa.team2.chatbot.dataref.ChatDataRefRepository;
import dev.jpa.team2.chatbot.embeddingchunk.EmbeddingChunkDto;
import dev.jpa.team2.chatbot.message.ChatMessage;
import dev.jpa.team2.chatbot.message.ChatMessageService;
import dev.jpa.team2.chatbot.messageref.ChatMessageRef;
import dev.jpa.team2.chatbot.messageref.ChatMessageRefRepository;
import dev.jpa.team2.chatbot.ragblocked.ChatRagBlockedChunkRepository;
import dev.jpa.team2.chatbot.session.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRagService {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final ChatDataRefRepository chatDataRefRepository;
    private final ChatMessageRefRepository chatMessageRefRepository;
    private final ChatRagRepository ragRepository;

    private final FastApiLlmService llmService;
    private final EmbeddingSimilarityService similarityService;

    private final ChatRagBlockedChunkRepository blockedChunkRepo;

    private static final int TOP_K = 5;

    // 후보 확장 검색 상한 (너무 크게 하면 비용↑)
    private static final int MAX_CANDIDATE_K = TOP_K * 20; // 100

    @Transactional
    public ChatRagDto ask(ChatRagDto dto, Long memberId) {

        Long sessionId = dto.getSessionId();
        String question = dto.getQuestion();

        // 세션 소유권 체크
        chatSessionService.requireOwnedSession(memberId, sessionId);

        // 1) 사용자 메시지 저장
        chatMessageService.saveMessage(memberId, sessionId, "USER", question);

        // 2) 세션 컨텍스트(ChatDataRef) 구성
        String sessionContext = chatDataRefRepository
            .findByMemberIdAndSessionIdOrderByCreatedAtDesc(memberId, sessionId)
            .stream()
            .map(ref -> String.format("[%s] %s\n%s", ref.getRefType(), ref.getTitle(), ref.getSummary()))
            .collect(Collectors.joining("\n\n"));

        // 3) 질문 임베딩
        List<Double> queryVector = llmService.embedding(question);

        // 4) ✅ 차단 chunk 목록
        List<Long> blockedIds = blockedChunkRepo.findActiveChunkIds();
        Set<Long> blockedSet = new HashSet<>(blockedIds);

        // 5) ✅ “부족하면 더 뽑아 채우기” (확장 검색 루프)
        List<EmbeddingChunkDto.SearchResult> topChunks =
            searchTopKWithBlockRetry(queryVector, blockedSet, TOP_K);

        log.info("[RagService] blocked={} topChunks={} sessionId={}",
            blockedSet.size(), topChunks.size(), sessionId);

        // 6) RAG context 만들기
        String ragContext = topChunks.stream()
            .map(EmbeddingChunkDto.SearchResult::getChunkText)
            .collect(Collectors.joining("\n\n"));

        String finalContext =
            "=== [세션 참고자료: 업로드/분석 결과 요약] ===\n" +
            (sessionContext == null || sessionContext.isBlank() ? "(없음)" : sessionContext) +
            "\n\n=== [RAG 검색 참고자료] ===\n" +
            (ragContext == null || ragContext.isBlank() ? "(없음)" : ragContext);

        // 7) LLM 호출
        String answer = llmService.chat(finalContext, question);

        // (옵션) RAG 결과 테이블 저장
        ragRepository.save(new ChatRag(sessionId, question, answer));

        // 8) 어시스턴트 메시지 저장
        ChatMessage assistantMsg = chatMessageService.saveMessage(memberId, sessionId, "ASSISTANT", answer);
        Long assistantChatId = assistantMsg.getChatId();

        // 9) ✅ chat_message_ref 저장: saveAll + distinct + rankNo
        saveRefsBatch(assistantChatId, topChunks);

        // 10) 응답 세팅
        dto.setAnswer(answer);
        dto.setReferences(topChunks);
        dto.setAssistantChatId(assistantChatId);
        return dto;
    }

    /**
     * 차단 chunk 제외 + chunkId 중복 제외 + TOP_K 부족 시 후보 K를 늘려 재검색
     */
    private List<EmbeddingChunkDto.SearchResult> searchTopKWithBlockRetry(
        List<Double> queryVector,
        Set<Long> blockedSet,
        int topK
    ) {
        // k = 20부터 시작해서 부족하면 40, 60... 늘림
        int candidateK = Math.min(MAX_CANDIDATE_K, topK * 4);

        List<EmbeddingChunkDto.SearchResult> result = List.of();

        while (true) {
            List<EmbeddingChunkDto.SearchResult> candidates =
                similarityService.searchTopK(queryVector, candidateK);

            // 더 이상 뽑을 게 없으면 종료
            if (candidates == null || candidates.isEmpty()) {
                return List.of();
            }

            // 순서 유지하면서 chunkId 기준 distinct + blocked 제외 + topK
            LinkedHashMap<Long, EmbeddingChunkDto.SearchResult> uniq = new LinkedHashMap<>();
            for (EmbeddingChunkDto.SearchResult r : candidates) {
                if (r == null || r.getChunkId() == null) continue;
                Long cid = r.getChunkId();
                if (blockedSet.contains(cid)) continue;
                uniq.putIfAbsent(cid, r);
                if (uniq.size() >= topK) break;
            }

            result = new ArrayList<>(uniq.values());
            if (result.size() >= topK) {
                return result;
            }

            // 후보를 더 늘려도 candidates 자체가 부족하면(=더 이상 확장 불가) 종료
            if (candidates.size() < candidateK) {
                return result; // 가능한 만큼만 반환
            }

            // candidateK 확장
            if (candidateK >= MAX_CANDIDATE_K) {
                return result;
            }
            candidateK = Math.min(MAX_CANDIDATE_K, candidateK + topK * 4);
        }
    }

    /**
     * ChatMessageRef를 rankNo 포함해서 saveAll로 한번에 저장.
     * (CHAT_ID, CHUNK_ID) 유니크가 있어도, 이 로직은 chunk 중복을 먼저 제거해 안전.
     */
    private void saveRefsBatch(Long assistantChatId, List<EmbeddingChunkDto.SearchResult> topChunks) {

        if (topChunks == null || topChunks.isEmpty()) {
            log.info("[RagService] refs skipped (empty) | chatId={}", assistantChatId);
            return;
        }

        // rankNo는 “필터링된 결과 순서” 기준으로 1..k
        int rankNo = 1;

        // distinct(혹시 같은 chunkId가 들어오는 경우 대비)
        LinkedHashMap<Long, EmbeddingChunkDto.SearchResult> uniq = new LinkedHashMap<>();
        for (EmbeddingChunkDto.SearchResult r : topChunks) {
            if (r == null || r.getChunkId() == null) continue;
            uniq.putIfAbsent(r.getChunkId(), r);
        }

        List<ChatMessageRef> refs = new ArrayList<>();
        for (EmbeddingChunkDto.SearchResult r : uniq.values()) {
            refs.add(ChatMessageRef.builder()
                .chatId(assistantChatId)
                .chunkId(r.getChunkId())
                .rankNo(rankNo++)
                .score(r.getSimilarityScore())
                .build());
        }

        try {
            chatMessageRefRepository.saveAll(refs);
            log.info("[RagService] refs saveAll ok | chatId={} count={}", assistantChatId, refs.size());
        } catch (Exception e) {
            // saveAll 실패 시에도 전체 ask를 죽이지 않기 위해 안전화
            log.error("[RagService] refs saveAll failed | chatId={} count={}", assistantChatId, refs.size(), e);

            // (선택) fallback: 하나씩 저장 시도
            int ok = 0, fail = 0;
            for (ChatMessageRef ref : refs) {
                try {
                    chatMessageRefRepository.save(ref);
                    ok++;
                } catch (Exception ex) {
                    fail++;
                }
            }
            log.error("[RagService] refs fallback save | chatId={} ok={} fail={}", assistantChatId, ok, fail);
        }
    }
}
