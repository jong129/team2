package dev.jpa.team2.chatbot.domain.session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import dev.jpa.team2.chatbot.FastApiLlmService;
import dev.jpa.team2.chatbot.domain.message.ChatMessage;
import dev.jpa.team2.chatbot.domain.message.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatSessionService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final FastApiLlmService llmService;
    
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
    
    public void ensureTitleUpdated(Long memberId, Long sessionId) {
      ChatSession session = requireOwnedSession(memberId, sessionId);

      String cur = (session.getTitle() == null) ? "" : session.getTitle().trim();
      if (!cur.isBlank() && !"새 대화".equals(cur)) return;

      // ✅ 1턴 기준(유저+AI): 메시지 2개 이상이면 생성
      long cnt = messageRepo.countBySessionId(sessionId);
      if (cnt < 2) return;

      // ✅ 앞 1~2턴만 사용(최대 4개 메시지)
      List<ChatMessage> msgs = messageRepo.findTop4BySessionIdOrderByCreatedAtAsc(sessionId);
      if (msgs == null || msgs.isEmpty()) return;

      StringBuilder sb = new StringBuilder();
      for (ChatMessage m : msgs) {
          String role = (m.getRole() == null) ? "" : m.getRole().trim();
          String content = (m.getContent() == null) ? "" : m.getContent().trim();
          if (content.isEmpty()) continue;

          String tag = "USER".equalsIgnoreCase(role) ? "USER" : "AI";
          sb.append(tag).append(": ").append(content).append("\n");
      }
      String raw = sb.toString().trim();
      if (raw.isEmpty()) return;

      String title = llmService.makeTitle(raw);
      if (title == null) return;

      title = title.replaceAll("[\"'\\.]", "").replaceAll("\\s+", " ").trim();
      if (title.isEmpty()) return;
      if (title.length() > 25) title = title.substring(0, 25).trim();

      session.setTitle(title);
      sessionRepo.save(session);

      log.info("[ChatSessionService] ensureTitleUpdated ok | memberId={} sessionId={} title={}",
          memberId, sessionId, title);
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
    
    public Map<String, Object> listMySessionsCursor(Long memberId, LocalDateTime cursor, int size) {
      int safeSize = Math.max(1, Math.min(size, 50));

      var page = sessionRepo.findMyActiveSessionsCursor(
          memberId,
          cursor,
          PageRequest.of(0, safeSize, Sort.by(Sort.Direction.DESC, "lastMessageAt"))
      );

      var items = page.getContent().stream()
          .map(ChatSessionDto.SessionItem::from)
          .toList();

      String nextCursor = null;
      if (!items.isEmpty()) {
          // 마지막 항목의 lastMessageAt을 nextCursor로
          var last = page.getContent().get(page.getContent().size() - 1);
          if (last.getLastMessageAt() != null) nextCursor = last.getLastMessageAt().toString();
      }

      return Map.of(
          "items", items,
          "nextCursor", nextCursor,
          "hasMore", page.hasNext()
      );
  }
}
