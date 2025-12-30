package dev.jpa.team2.chatbot.dataref;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.session.ChatSession;
import dev.jpa.team2.chatbot.session.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j   
@Service
@RequiredArgsConstructor
@Transactional
public class ChatDataRefService {

    private final ChatDataRefRepository chatDataRefRepository;
    private final ChatSessionRepository chatSessionRepository;

    public ChatDataRefDto.Response attachToLatestSession(Long memberId, ChatDataRefDto.Request req) {

        if (req == null || req.getRefType() == null || req.getTitle() == null || req.getSummary() == null) {
            throw new IllegalArgumentException("refType/title/summary는 필수입니다.");
        }

        ChatSession session = getOrCreateLatestActiveSession(memberId);
        
        log.debug(
            "[ChatDataRefService] saving ref | memberId={} | sessionId={} | refType={}",
            memberId,
            session.getSessionId(),
            req.getRefType()
        );
        
        ChatDataRef saved = chatDataRefRepository.save(
            ChatDataRef.builder()
                .memberId(memberId)
                .sessionId(session.getSessionId())
                .refType(req.getRefType())
                .title(req.getTitle())
                .summary(req.getSummary())
                .createdAt(LocalDateTime.now())
                .build()
        );

        session.setLastMessageAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        return ChatDataRefDto.Response.builder()
            .success(true)
            .sessionId(session.getSessionId())
            .refId(saved.getRefId())
            .build();
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
}
