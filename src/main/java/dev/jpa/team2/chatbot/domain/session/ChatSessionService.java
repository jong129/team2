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
        String t = (title == null || title.trim().isEmpty()) ? "새 대화" : title.trim(); // title이 비어 있으면 새 대화로 강제

        ChatSession s = new ChatSession();
        s.setMemberId(memberId);
        s.setTitle(t);
        s.setSessionStatus("ACTIVE"); // 세션 상태 ACTIVE
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

    // 세션 소유권 검증
    public ChatSession requireOwnedSession(Long memberId, Long sessionId) {
        return sessionRepo.findBySessionIdAndMemberId(sessionId, memberId)
            .orElseThrow(() -> new RuntimeException("세션이 없거나 권한이 없습니다. sessionId=" + sessionId));
    }

    // lastMessageAt 업데이트 : 메시지가 추가될 떄 세션이 위로 올라오게 하는 기능
    public void touchLastMessageAt(ChatSession s) {
        s.setLastMessageAt(LocalDateTime.now());
        sessionRepo.save(s);
    }
    
    // 세션 제목 자동 생성 : 대화가 조금 쌓이면 제목을 LLM으로 자동 생성
    public void ensureTitleUpdated(Long memberId, Long sessionId) {
      // 1) 소유권 확인
      ChatSession session = requireOwnedSession(memberId, sessionId);
      
      // 2) 현재 제목 검사
      String cur = (session.getTitle() == null) ? "" : session.getTitle().trim();
      if (!cur.isBlank() && !"새 대화".equals(cur)) return;
      
      // 3) 메시지 개수 조건 : 1턴 기준(유저+AI): 메시지 2개 이상이면 생성
      long cnt = messageRepo.countBySessionId(sessionId);
      if (cnt < 2) return;
      
      // 4) 제목 생성에 사용할 메시지 샘플 : 앞 1~2턴만 사용(최대 4개 메시지) -> 토큰 절약 + 대표성
      List<ChatMessage> msgs = messageRepo.findTop4BySessionIdOrderByCreatedAtAsc(sessionId);
      if (msgs == null || msgs.isEmpty()) return;
      
      // 5) 프롬프트용 문자열 구성
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
      
      // 6) LLM 호출
      String title = llmService.makeTitle(raw);
      if (title == null) return;
      
      // 7) 후처리(클린업) 
      title = title.replaceAll("[\"'\\.]", "").replaceAll("\\s+", " ").trim();  // 따옴표/마침표 제거
      if (title.isEmpty()) return;  // 공백 정리
      if (title.length() > 25) title = title.substring(0, 25).trim(); // 길이 25자 제한

      // 8) 저장 후 로그
      session.setTitle(title);
      sessionRepo.save(session);
      log.info("[ChatSessionService] ensureTitleUpdated ok | memberId={} sessionId={} title={}",
          memberId, sessionId, title);
  }


    // 날짜별 세션 그룹 (readOnly)
    @Transactional(readOnly = true)
    public List<ChatSessionDto.GroupedByDate<ChatSessionDto.SessionItem>> getGroupedSessions(Long memberId) {
        // ACTIVE 세션을 최신순으로 가져오기
        List<ChatSession> sessions = sessionRepo.findByMemberIdAndSessionStatusOrderByLastMessageAtDesc(memberId, "ACTIVE");

        Map<String, List<ChatSessionDto.SessionItem>> map = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        
        // lastMessageAt(없으면 startTime) 기준으로 날짜(YYYY-MM-DD) 그룹핑
        for (ChatSession s : sessions) {
            LocalDate d = (s.getLastMessageAt() != null ? s.getLastMessageAt() : s.getStartTime()).toLocalDate();
            String key = d.format(fmt);

            map.computeIfAbsent(key, k -> new ArrayList<>())
               .add(ChatSessionDto.SessionItem.from(s));
        }
        
        // GroupedByDate<SessionItem> 리스트 반환
        List<ChatSessionDto.GroupedByDate<ChatSessionDto.SessionItem>> out = new ArrayList<>();
        for (var e : map.entrySet()) {
            out.add(new ChatSessionDto.GroupedByDate<>(e.getKey(), e.getValue()));
        }

        log.info("[ChatSessionService] getGroupedSessions ok | memberId={} days={} sessions={}",
                memberId, out.size(), sessions.size());

        return out;
    }

    // 세션 소프트 삭제 : 진짜 삭제가 아니라 상태만 바꿔서 이력 보존
    public void softDelete(Long memberId, Long sessionId) {
        ChatSession s = requireOwnedSession(memberId, sessionId);
        s.setSessionStatus("DELETED");
        s.setDeletedAt(LocalDateTime.now());
        sessionRepo.save(s);

        log.info("[ChatSessionService] softDelete ok | memberId={} sessionId={}", memberId, sessionId);
    }
    
    // 커서 기반 세션 목록    
    public Map<String, Object> listMySessionsCursor(Long memberId, LocalDateTime cursor, int size) {
      int safeSize = Math.max(1, Math.min(size, 50)); // size를 1~50으로 제한(안전장치)

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
          "items", items, // SessionItem 리스트
          "nextCursor", nextCursor, // 마지막 아이템의 lastMessageAt.toString()
          "hasMore", page.hasNext()
      );
  }
}
