package dev.jpa.team2.chatbot.admin;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.message.ChatMessage;
import dev.jpa.team2.chatbot.message.ChatMessageRepository;
import dev.jpa.team2.chatbot.session.ChatSession;
import dev.jpa.team2.chatbot.session.ChatSessionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;

    @Transactional(readOnly = true)
    public Page<ChatSession> listSessions(String status, Long memberId, String q,
                                         LocalDateTime from, LocalDateTime to,
                                         int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        String qq = (q == null || q.trim().isEmpty()) ? null : q.trim();
        String st = (status == null || status.trim().isEmpty()) ? null : status.trim();

        return sessionRepo.adminFindSessions(st, memberId, qq, from, to, pageable);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> loadMessages(Long sessionId) {
        // 관리자라서 소유 검증 없음
        return messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    public void softDelete(Long sessionId) {
        ChatSession s = sessionRepo.findBySessionId(sessionId);
        if (s == null) throw new IllegalArgumentException("session not found");
        s.setSessionStatus("DELETED");
        s.setDeletedAt(LocalDateTime.now());
        sessionRepo.save(s);
    }

    public void restore(Long sessionId) {
        ChatSession s = sessionRepo.findBySessionId(sessionId);
        if (s == null) throw new IllegalArgumentException("session not found");
        s.setSessionStatus("ACTIVE");
        s.setDeletedAt(null);
        sessionRepo.save(s);
    }

    // (옵션) 하드 삭제
    public void hardDelete(Long sessionId) {
        // 메시지 먼저 삭제(외래키 있으면 필요)
        messageRepo.deleteAll(messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId));
        sessionRepo.deleteById(sessionId);
    }

    // (옵션) 메시지 수
    @Transactional(readOnly = true)
    public Map<Long, Long> countMessagesBulk(List<Long> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) return Map.of();

        List<Object[]> rows = messageRepo.countBySessionIds(sessionIds);
        Map<Long, Long> map = new HashMap<>();

        for (Object[] r : rows) {
            Long sid = (Long) r[0];
            Long cnt = (Long) r[1];
            map.put(sid, cnt);
        }
        return map;
    }

}
