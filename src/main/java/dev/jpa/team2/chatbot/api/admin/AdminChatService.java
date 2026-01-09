package dev.jpa.team2.chatbot.api.admin;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.domain.message.ChatMessage;
import dev.jpa.team2.chatbot.domain.message.ChatMessageRepository;
import dev.jpa.team2.chatbot.domain.session.ChatSession;
import dev.jpa.team2.chatbot.domain.session.ChatSessionRepository;
import lombok.RequiredArgsConstructor;

// 관리자 화면에서 필요한 세션 목록 조회 / 메시지 조회 / 소프트삭제 및 복구 / 하드 삭제 / 세션별 메시지 수 집계를 처리하는 서비스

@Service
@RequiredArgsConstructor
@Transactional
public class AdminChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;

    // 관리자 세션 목록 조회 (페이지) : 관리자용 검색 조건을 받아 Page<ChatSession> 형태로 페이징된 세션 목록을 반환
    @Transactional(readOnly = true)
    public Page<ChatSession> listSessions(String status, Long memberId, String q,
                                         LocalDateTime from, LocalDateTime to,
                                         int page, int size) {
        int safePage = Math.max(0, page); //  음수 페이지 방지
        int safeSize = Math.max(1, Math.min(size, 100));  // 사이즈 최대 100 제한으로 DB 부하/응답 크기 제한
        Pageable pageable = PageRequest.of(safePage, safeSize);
        
        // 검색어, 상태 문자열 정리
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
