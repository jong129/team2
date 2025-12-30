package dev.jpa.team2.chatbot.rag;

import java.util.List;
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
import dev.jpa.team2.chatbot.session.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final ChatDataRefRepository chatDataRefRepository;
    private final ChatMessageRefRepository chatMessageRefRepository;
    private final RagRepository ragRepository;

    private final FastApiLlmService llmService;
    private final EmbeddingSimilarityService similarityService;

    private static final int TOP_K = 5;

    @Transactional
    public RagDto ask(RagDto dto, Long memberId) {

        Long sessionId = dto.getSessionId();
        String question = dto.getQuestion();

        // 세션 소유권 체크
        chatSessionService.requireOwnedSession(memberId, sessionId);

        // 사용자 메시지 저장
        ChatMessage userMsg = chatMessageService.saveMessage(memberId, sessionId, "USER", question);

        // 세션 컨텍스트(ChatDataRef) 구성
        String sessionContext = chatDataRefRepository
            .findByMemberIdAndSessionIdOrderByCreatedAtDesc(memberId, sessionId)
            .stream()
            .map(ref -> String.format("[%s] %s\n%s", ref.getRefType(), ref.getTitle(), ref.getSummary()))
            .collect(Collectors.joining("\n\n"));

        log.info("[RagService] SESSION_CONTEXT_LEN={} sessionId={}",
                sessionContext == null ? -1 : sessionContext.length(), sessionId);

        // 질문 임베딩
        List<Double> queryVector = llmService.embedding(question);

        // Top-K chunk 검색 (embedding_chunk가 비어있으면 여기서 0개)
        List<EmbeddingChunkDto.SearchResult> topChunks = similarityService.searchTopK(queryVector, TOP_K);

        log.info("[RagService] topChunks size={} sessionId={}", topChunks.size(), sessionId);

        // RAG context 만들기
        String ragContext = topChunks.stream()
            .map(EmbeddingChunkDto.SearchResult::getChunkText)
            .collect(Collectors.joining("\n\n"));

        String finalContext =
            "=== [세션 참고자료: 업로드/분석 결과 요약] ===\n" +
            (sessionContext == null || sessionContext.isBlank() ? "(없음)" : sessionContext) +
            "\n\n=== [RAG 검색 참고자료] ===\n" +
            (ragContext == null || ragContext.isBlank() ? "(없음)" : ragContext);

        // LLM 호출
        String answer = llmService.chat(finalContext, question);
        
        // RAG 결과 테이블 저장 (질문/답변 이력)
        ragRepository.save(new Rag(sessionId, question, answer));

        // 어시스턴트 메시지 저장
        ChatMessage assistantMsg = chatMessageService.saveMessage(memberId, sessionId, "ASSISTANT", answer);

        // chat_message_ref 저장(근거 연결) - try/catch로 안전화
        Long assistantChatId = assistantMsg.getChatId();

        int ok = 0;
        int fail = 0;

        for (EmbeddingChunkDto.SearchResult chunk : topChunks) {
            try {
                chatMessageRefRepository.save(
                    ChatMessageRef.builder()
                        .chatId(assistantChatId)
                        .chunkId(chunk.getChunkId())
                        .score(chunk.getSimilarityScore())
                        .build()
                );
                ok++;
            } catch (Exception e) {
                fail++;
                log.error("[RagService] save ChatMessageRef failed | chatId={} chunkId={}",
                        assistantChatId, chunk.getChunkId(), e);
            }
        }

        log.info("[RagService] refs saved | chatId={} ok={} fail={}", assistantChatId, ok, fail);

        dto.setAnswer(answer);
        dto.setReferences(topChunks);
        return dto;
    }
}
