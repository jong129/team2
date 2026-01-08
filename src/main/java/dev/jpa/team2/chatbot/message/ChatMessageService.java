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

    // 메시지 저장 (레거시 유지)
    public ChatMessage saveMessage(Long memberId, Long sessionId, String role, String content) {
        ChatSession s = sessionService.requireOwnedSession(memberId, sessionId);

        ChatMessage msg = ChatMessage.of(memberId, sessionId, role, content);
        ChatMessage saved = messageRepo.save(msg);

        sessionService.touchLastMessageAt(s);

        // "새 대화" 제목 자동 생성은 사용자 메시지 저장 후에만 실행
        if ("user".equalsIgnoreCase(role)) {
            sessionService.ensureTitleUpdated(memberId, sessionId);
        }

        log.info("[ChatMessageService] saveMessage ok | memberId={} sessionId={} chatId={} role={}",
                memberId, sessionId, saved.getChatId(), role);

        return saved;
    }

    /**
     * ✅ 추가: ASSISTANT 메시지 저장 + 추천질문 3개까지 함께 저장
     * - followUps는 0~3개 들어와도 됨
     */
    public ChatMessage saveAssistantMessageWithFollowUps(Long memberId, Long sessionId, String content, List<String> followUps) {
        ChatSession s = sessionService.requireOwnedSession(memberId, sessionId);

        ChatMessage msg = ChatMessage.of(memberId, sessionId, "ASSISTANT", content);

        if (followUps != null) {
            msg.setSuggestQ1(followUps.size() > 0 ? safeTrim(followUps.get(0)) : null);
            msg.setSuggestQ2(followUps.size() > 1 ? safeTrim(followUps.get(1)) : null);
            msg.setSuggestQ3(followUps.size() > 2 ? safeTrim(followUps.get(2)) : null);
        }

        ChatMessage saved = messageRepo.save(msg);
        sessionService.touchLastMessageAt(s);

        log.info("[ChatMessageService] saveAssistantMessageWithFollowUps ok | memberId={} sessionId={} chatId={}",
                memberId, sessionId, saved.getChatId());

        return saved;
    }

    /**
     * ✅ 추가: 이미 저장된 메시지(chatId)에 추천질문만 업데이트
     */
    public void updateSuggestQuestions(Long memberId, Long sessionId, Long chatId, List<String> followUps) {
        sessionService.requireOwnedSession(memberId, sessionId);

        ChatMessage m = messageRepo.findById(chatId)
            .orElseThrow(() -> new NoSuchElementException("ChatMessage not found: " + chatId));

        if (followUps == null) followUps = List.of();

        m.setSuggestQ1(followUps.size() > 0 ? safeTrim(followUps.get(0)) : null);
        m.setSuggestQ2(followUps.size() > 1 ? safeTrim(followUps.get(1)) : null);
        m.setSuggestQ3(followUps.size() > 2 ? safeTrim(followUps.get(2)) : null);

        // @Transactional이라 save 안 해도 dirty checking으로 반영됨
        log.info("[ChatMessageService] updateSuggestQuestions ok | chatId={} qCount={}", chatId, Math.min(3, followUps.size()));
    }

    private String safeTrim(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isBlank() ? null : s;
    }

    // 키워드 검색 (날짜별 그룹)
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
