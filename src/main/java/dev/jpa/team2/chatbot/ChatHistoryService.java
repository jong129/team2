package dev.jpa.team2.chatbot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatHistoryService {

    private final ChatHistoryRepository messageRepo;
    private final ChatSessionService sessionService;

    public ChatMessagesResponseDto loadSessionMessages(Long memberId, Long sessionId) {
        // ✅ 내 세션인지 확인
        sessionService.requireOwnedSession(memberId, sessionId);

        List<ChatHistory> list = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<ChatMessageDto> dtos = list.stream().map(ChatMessageDto::from).toList();
        return new ChatMessagesResponseDto(sessionId, dtos);
    }

    public ChatHistory saveMessage(Long memberId, Long sessionId, String role, String content) {
        // ✅ 내 세션인지 확인
        ChatSession s = sessionService.requireOwnedSession(memberId, sessionId);

        ChatHistory msg = ChatHistory.of(sessionId, role, content);
        ChatHistory saved = messageRepo.save(msg);

        // 세션 최근시간 업데이트
        sessionService.touchLastMessageAt(s);

        return saved;
    }

    public List<GroupedSearchResultsDto> searchMyMessages(Long memberId, String keyword, int size) {
        if (keyword == null) keyword = "";
        keyword = keyword.trim();
        if (keyword.isEmpty()) return List.of();

        List<ChatHistory> found = messageRepo.searchMyMessages(memberId, keyword, PageRequest.of(0, size));

        // 날짜별 그룹
        Map<String, List<SearchResultDto>> map = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;

        for (ChatHistory m : found) {
            LocalDate d = m.getCreatedAt().toLocalDate();
            String key = d.format(fmt);

            SearchResultDto dto = new SearchResultDto(
                m.getSessionId(),
                m.getChatId(),
                m.getRole(),
                m.getContent(),
                m.getCreatedAt()
            );

            map.computeIfAbsent(key, k -> new ArrayList<>()).add(dto);
        }

        List<GroupedSearchResultsDto> out = new ArrayList<>();
        for (var e : map.entrySet()) {
            out.add(new GroupedSearchResultsDto(e.getKey(), e.getValue()));
        }
        return out;
    }
}
