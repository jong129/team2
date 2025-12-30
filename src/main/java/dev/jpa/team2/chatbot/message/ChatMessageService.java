package dev.jpa.team2.chatbot.message;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.session.ChatSession;
import dev.jpa.team2.chatbot.session.ChatSessionDto;
import dev.jpa.team2.chatbot.session.ChatSessionService;
import dev.jpa.team2.chatbot.session.ChatSessionDto.GroupedByDate;
import dev.jpa.team2.chatbot.session.ChatSessionDto.SearchResultItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository messageRepo;
    private final ChatSessionService sessionService;

    // 세션 메시지 전체 로딩
    @Transactional(readOnly = true)
    public ChatMessagesResponseDto loadSessionMessages(Long memberId, Long sessionId) {
        sessionService.requireOwnedSession(memberId, sessionId);

        List<ChatMessage> list = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<ChatMessageDto> dtos = list.stream().map(ChatMessageDto::from).toList();

        log.info("[ChatMessageService] loadSessionMessages ok | memberId={} sessionId={} count={}",
                memberId, sessionId, dtos.size());

        return new ChatMessagesResponseDto(sessionId, dtos);
    }

    // 메시지 저장
    public ChatMessage saveMessage(Long memberId, Long sessionId, String role, String content) {
        ChatSession s = sessionService.requireOwnedSession(memberId, sessionId);

        ChatMessage msg = ChatMessage.of(sessionId, role, content);
        ChatMessage saved = messageRepo.save(msg);

        sessionService.touchLastMessageAt(s);

        log.info("[ChatMessageService] saveMessage ok | memberId={} sessionId={} chatId={} role={}",
                memberId, sessionId, saved.getChatId(), role);

        return saved;
    }

    // 키워드 검색 (날짜별 그룹)  -> 통합 DTO 기준!
    @Transactional(readOnly = true)
    public List<ChatSessionDto.GroupedByDate<ChatSessionDto.SearchResultItem>> searchMyMessages(
        Long memberId, String keyword, int size
    ) {
        if (keyword == null) keyword = "";
        keyword = keyword.trim();
        if (keyword.isEmpty()) return List.of();

        int safeSize = Math.max(1, Math.min(size, 200));

        List<ChatMessage> found = messageRepo.searchMyMessages(memberId, keyword, PageRequest.of(0, safeSize));

        Map<String, List<ChatSessionDto.SearchResultItem>> map = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;

        for (ChatMessage m : found) {
            LocalDate d = m.getCreatedAt().toLocalDate();
            String key = d.format(fmt);

            ChatSessionDto.SearchResultItem dto = new ChatSessionDto.SearchResultItem(
                m.getSessionId(),
                m.getChatId(),
                m.getRole(),
                m.getContent(),
                m.getCreatedAt()
            );

            map.computeIfAbsent(key, k -> new ArrayList<>()).add(dto);
        }

        List<ChatSessionDto.GroupedByDate<ChatSessionDto.SearchResultItem>> out = new ArrayList<>();
        for (var e : map.entrySet()) {
            out.add(new ChatSessionDto.GroupedByDate<>(e.getKey(), e.getValue()));
        }

        log.info("[ChatMessageService] searchMyMessages ok | memberId={} keyword='{}' hits={} days={}",
                memberId, keyword, found.size(), out.size());

        return out;
    }
}
