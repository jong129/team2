package dev.jpa.team2.chatbot.domain.dataref;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.domain.embeddingchunk.EmbeddingChunkService;
import dev.jpa.team2.chatbot.domain.session.ChatSession;
import dev.jpa.team2.chatbot.domain.session.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 컨텍스트를 최신 ACTIVE 세션에 붙여 저장 + 요약 텍스트를 청크/임베딩으로 만들어 RAG 준비

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatDataRefService {

    private final ChatDataRefRepository chatDataRefRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final EmbeddingChunkService embeddingChunkService; // embedding 저장 서비스
    
    public ChatDataRefDto.Response attachToLatestSession(Long memberId, ChatDataRefDto.Request req) {
        // 요청값 검증
        if (req == null || req.getRefType() == null || req.getTitle() == null || req.getSummary() == null) {
            throw new IllegalArgumentException("refType/title/summary는 필수입니다.");
        }
        
        // 최신 ACTIVE 세션 가져오기(없으면 생성)
        ChatSession session = getOrCreateLatestActiveSession(memberId);

        log.debug("[ChatDataRefService] saving ref | memberId={} | sessionId={} | refType={}",
                memberId, session.getSessionId(), req.getRefType());

        ChatDataRef saved = chatDataRefRefSave(memberId, session, req); // ChatDataRef 저장

        // ref 저장 직후, embedding_chunk 자동 생성
        try {
            int inserted = embeddingChunkService.saveChunksFromText(saved.getDataRefId(), saved.getSummary());
            log.info("[ChatDataRefService] embedding chunks inserted={} | refId={} | sessionId={}",
                    inserted, saved.getDataRefId(), session.getSessionId());
        } catch (Exception e) {
            // 실패해도 ref 자체는 저장된 상태 유지 (서비스 전체가 죽지 않게)
            log.error("[ChatDataRefService] embedding chunk generation failed | refId={}", saved.getDataRefId(), e);
        }

        // 세션 lastMessageAt 갱신
        session.setLastMessageAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        return ChatDataRefDto.Response.builder()
            .success(true)
            .sessionId(session.getSessionId())
            .refId(saved.getDataRefId())
            .build();
    }

    private ChatDataRef chatDataRefRefSave(Long memberId, ChatSession session, ChatDataRefDto.Request req) {
        return chatDataRefRepository.save(
            ChatDataRef.builder()
                .memberId(memberId)
                .sessionId(session.getSessionId())
                .refType(req.getRefType())
                .title(req.getTitle())
                .summary(req.getSummary())
                .createdAt(LocalDateTime.now())
                .build()
        );
    }

    private ChatSession getOrCreateLatestActiveSession(Long memberId) {
        Optional<ChatSession> latest =
            chatSessionRepository.findTopByMemberIdAndSessionStatusOrderByLastMessageAtDesc(memberId, "ACTIVE");

        if (latest.isPresent()) return latest.get();

        ChatSession created = new ChatSession();
        created.setMemberId(memberId);
        created.setSessionStatus("ACTIVE");
        created.setTitle("새 대화");
        created.setStartTime(LocalDateTime.now());
        created.setLastMessageAt(LocalDateTime.now());

        return chatSessionRepository.save(created);
    }
    
//    // 참고
//    @Transactional(readOnly = true)
//    public ChatMessageDto getHistory(Long memberId, Long sessionId) {
//        // 세션 소유권 체크(중복이어도 안전)
//        ChatSessionService.requireOwnedSession(memberId, sessionId);
//
//        // 기존 ChatMessageService를 활용해서 세션 메시지 조회
//        return ChatMessageService.loadSessionMessages(memberId, sessionId);
//    }

}
