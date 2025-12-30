package dev.jpa.team2.chatbot.session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatSessionService {

    private final ChatSessionRepository sessionRepo;

    // 세션 생성
    public ChatSession createSession(Long memberId, String title) {
        String t = (title == null || title.trim().isEmpty()) ? "새 대화" : title.trim();

        ChatSession s = new ChatSession();
        s.setMemberId(memberId);
        s.setTitle(t);
        s.setSessionStatus("ACTIVE");
        s.setStartTime(LocalDateTime.now());
        s.setLastMessageAt(LocalDateTime.now());

        ChatSession saved = sessionRepo.save(s);

        log.info("[ChatSessionService] createSession ok | memberId={} sessionId={} title={}",
                memberId, saved.getSessionId(), saved.getTitle());

        return saved;
    }

    // 최근 ACTIVE 세션(없으면 생성)
    public ChatSession getOrCreateLatestActiveSession(Long memberId) {
        return sessionRepo
            .findTopByMemberIdAndSessionStatusOrderByLastMessageAtDesc(memberId, "ACTIVE")
            .orElseGet(() -> createSession(memberId, "새 대화"));
    }

    // 세션 소유 검증
    public ChatSession requireOwnedSession(Long memberId, Long sessionId) {
        return sessionRepo.findBySessionIdAndMemberId(sessionId, memberId)
            .orElseThrow(() -> new RuntimeException("세션이 없거나 권한이 없습니다. sessionId=" + sessionId));
    }

    // 최근 시간 업데이트
    public void touchLastMessageAt(ChatSession s) {
        s.setLastMessageAt(LocalDateTime.now());
        sessionRepo.save(s);
    }

    // 날짜별 세션 그룹 (aibotpage)
    @Transactional(readOnly = true)
    public List<ChatSessionDto.GroupedByDate<ChatSessionDto.SessionItem>> getGroupedSessions(Long memberId) {

        List<ChatSession> sessions =
            sessionRepo.findByMemberIdAndSessionStatusOrderByLastMessageAtDesc(memberId, "ACTIVE");

        Map<String, List<ChatSessionDto.SessionItem>> map = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;

        for (ChatSession s : sessions) {
            LocalDate d = (s.getLastMessageAt() != null ? s.getLastMessageAt() : s.getStartTime()).toLocalDate();
            String key = d.format(fmt);

            map.computeIfAbsent(key, k -> new ArrayList<>())
               .add(ChatSessionDto.SessionItem.from(s));
        }

        List<ChatSessionDto.GroupedByDate<ChatSessionDto.SessionItem>> out = new ArrayList<>();
        for (var e : map.entrySet()) {
            out.add(new ChatSessionDto.GroupedByDate<>(e.getKey(), e.getValue()));
        }

        log.info("[ChatSessionService] getGroupedSessions ok | memberId={} days={} sessions={}",
                memberId, out.size(), sessions.size());

        return out;
    }

    // 세션 삭제
    public void softDelete(Long memberId, Long sessionId) {
        ChatSession s = requireOwnedSession(memberId, sessionId);
        s.setSessionStatus("DELETED");
        s.setDeletedAt(LocalDateTime.now());
        sessionRepo.save(s);

        log.info("[ChatSessionService] softDelete ok | memberId={} sessionId={}", memberId, sessionId);
    }
}
