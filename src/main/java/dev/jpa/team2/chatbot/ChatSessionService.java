package dev.jpa.team2.chatbot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatSessionService {

    private final ChatSessionRepository sessionRepo;

    public ChatSession getOrCreateLatestSession(Long memberId) {
        return sessionRepo
            .findTopByMemberIdAndSessionStatusOrderByLastMessageAtDesc(memberId, "ACTIVE")
            .orElseGet(() -> {
                ChatSession s = new ChatSession();
                s.setMemberId(memberId);
                s.setTitle("새 대화");
                s.setSessionStatus("ACTIVE");
                return sessionRepo.save(s);
            });
    }

    public List<GroupedSessionsDto> getGroupedSessions(Long memberId) {
        List<ChatSession> sessions =
            sessionRepo.findByMemberIdAndSessionStatusOrderByLastMessageAtDesc(memberId, "ACTIVE");

        // 날짜별 그룹
        Map<String, List<ChatSessionDto>> map = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;

        for (ChatSession s : sessions) {
            LocalDate d = (s.getLastMessageAt() != null ? s.getLastMessageAt() : s.getStartTime()).toLocalDate();
            String key = d.format(fmt);
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(ChatSessionDto.from(s));
        }

        List<GroupedSessionsDto> out = new ArrayList<>();
        for (var e : map.entrySet()) {
            out.add(new GroupedSessionsDto(e.getKey(), e.getValue()));
        }
        return out;
    }

    public ChatSession requireOwnedSession(Long memberId, Long sessionId) {
        return sessionRepo.findBySessionIdAndMemberId(sessionId, memberId)
            .orElseThrow(() -> new RuntimeException("세션이 없거나 권한이 없습니다."));
    }

    public void touchLastMessageAt(ChatSession s) {
        s.setLastMessageAt(java.time.LocalDateTime.now());
        sessionRepo.save(s);
    }

    public void softDelete(Long memberId, Long sessionId) {
        ChatSession s = requireOwnedSession(memberId, sessionId);
        s.setSessionStatus("DELETED");
        s.setDeletedAt(java.time.LocalDateTime.now());
        sessionRepo.save(s);
    }
}
