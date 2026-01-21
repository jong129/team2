package dev.jpa.team2.chatbot.domain.message;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.api.response.ChatMessagesResponseDto;
import dev.jpa.team2.chatbot.domain.feedback.ChatMessageFeedbackRepository;
import dev.jpa.team2.chatbot.domain.session.ChatSession;
import dev.jpa.team2.chatbot.domain.session.ChatSessionDto;
import dev.jpa.team2.chatbot.domain.session.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 1. 세션 메시지 로딩 + 내 피드백 붙이기
// 2. 메시지 저장(유저/어시스턴트) + 추천질문/사용량 저장 + 세션 메타 업데이트
// 3. 키워드 검색(내 메시지) + 날짜별 그룹핑

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository messageRepo;
    private final ChatSessionService sessionService;
    private final ChatMessageFeedbackRepository feedbackRepo;

    // 세션 메시지 전체 로딩 + myFeedback 붙이기
    @Transactional(readOnly = true)
    public ChatMessagesResponseDto loadSessionMessages(Long memberId, Long sessionId) {
      // 소유권 검증  
      sessionService.requireOwnedSession(memberId, sessionId);

      // 세션의 메시지 전부 조회
      List<ChatMessage> list = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
      // 엔티티 -> DTO 변환 
      List<ChatMessageDto> dtos = list.stream().map(ChatMessageDto::from).toList();

      // 1) chatId 리스트 추출
      List<Long> chatIds = list.stream()
          .map(ChatMessage::getChatId)
          .filter(Objects::nonNull)
          .toList();

      if (!chatIds.isEmpty()) {
          // 2) 내 피드백을 한 번에 조회(bulk) : N+1 방지용
          List<Object[]> rows = feedbackRepo.findMyFeedbackByChatIds(memberId, chatIds);

          // 3) chatId -> myFeedback(1/-1) Map으로 변환
          Map<Long, Integer> myFbMap = new HashMap<>();
          for (Object[] r : rows) { 
            Long chatId = r[0] == null ? null : ((Number) r[0]).longValue();
            Integer val = r[1] == null ? null : ((Number) r[1]).intValue();

            if (chatId == null) continue;

            myFbMap.put(chatId, val);
          }

          // 4) DTO에 myFeedback 주입
          for (ChatMessageDto dto : dtos) {  
            dto.setMyFeedback(myFbMap.get(dto.getChatId())); // 없으면 null
          }
      }

      return new ChatMessagesResponseDto(sessionId, dtos);
    }


    // 메시지 저장 (레거시 유지) : 세션 목록 화면에서 최근 대화시간이 바로 갱신됨. 제목 자동 생성은 유저가 첫 질문을 던지 시점 기준.
    public ChatMessage saveMessage(Long memberId, Long sessionId, String role, String content) {
      // 1) 세션 소유권 확인 + 세션 엔티티 획득
      ChatSession s = sessionService.requireOwnedSession(memberId, sessionId);
      
      // 2) 메시지 엔티티 생성 후 저장
      ChatMessage msg = ChatMessage.of(memberId, sessionId, role, content);  
      ChatMessage saved = messageRepo.save(msg);
      
      // 3) 세션의 마지막 메시지 시간 갱신
      sessionService.touchLastMessageAt(s);

      // 4) 유저 메시지일 때만 새 대화 제목 자동 생성
      if ("USER".equalsIgnoreCase(role)) {   
        sessionService.ensureTitleUpdated(memberId, sessionId);
      }

      log.info("[ChatMessageService] saveMessage ok | memberId={} sessionId={} chatId={} role={}",
              memberId, sessionId, saved.getChatId(), role);

      return saved;
    }

    // ASSISTANT 메시지 저장 + 추천질문 3개 저장 : ASSISTANT role로 메시지 저장. followUps 최대 3개를 suggestQ1~Q3에 저장. lastMessageAt 갱신
    // followUps는 0~3개 들어와도 됨
    public ChatMessage saveAssistantMessageWithFollowUps(Long memberId, Long sessionId, String content, List<String> followUps) {
      ChatSession s = sessionService.requireOwnedSession(memberId, sessionId);

      ChatMessage msg = ChatMessage.of(memberId, sessionId, "ASSISTANT", content);

      if (followUps != null) {    // safeTrim : 추천질문 문자열의 공백/빈값을 null 처리하려는 유틸
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

    // ASSISTANT 메시지 저장 + 추천질문 + model/tokens/latency 저장
    public ChatMessage saveAssistantMessageWithFollowUpsAndUsage(Long memberId, Long sessionId, String content, List<String> followUps,
                                                                                String model, Integer tokensIn, Integer tokensOut, Integer tokensTotal, Integer latencyMs) {  
      ChatSession s = sessionService.requireOwnedSession(memberId, sessionId);

      ChatMessage msg = ChatMessage.of(memberId, sessionId, "ASSISTANT", content);

      // 추천질문 (SUGGEST_Q1~Q3)
      if (followUps != null) {
        msg.setSuggestQ1(followUps.size() > 0 ? safeTrim(followUps.get(0)) : null);
        msg.setSuggestQ2(followUps.size() > 1 ? safeTrim(followUps.get(1)) : null);
        msg.setSuggestQ3(followUps.size() > 2 ? safeTrim(followUps.get(2)) : null);
      }

      // usage 저장 (CHAT_MESSAGE.MODEL, TOKENS_IN, TOKENS_OUT, TOKENS_TOTAL, LATENCY_MS)
      if (model != null && !model.isBlank()) {
        msg.setModel(model.trim());
      }

      msg.setTokensIn(tokensIn);
      msg.setTokensOut(tokensOut);

      // tokensTotal 없으면 in+out으로 계산해서 채움
      Integer total = tokensTotal;
      if (total == null && tokensIn != null && tokensOut != null) {
          total = tokensIn + tokensOut;
      }
      msg.setTokensTotal(total);

      msg.setLatencyMs(latencyMs);

      ChatMessage saved = messageRepo.save(msg);
      sessionService.touchLastMessageAt(s);

      log.info(
          "[ChatMessageService] saveAssistantMessageWithFollowUpsAndUsage ok | memberId={} sessionId={} chatId={} role={} model={} tokensIn={} tokensOut={} tokensTotal={} latencyMs={}",
          memberId, sessionId, saved.getChatId(), saved.getRole(), saved.getModel(), saved.getTokensIn(), saved.getTokensOut(), saved.getTokensTotal(), saved.getLatencyMs()
      );

      return saved;
    }

    
    // 이미 저장된 메시지(chatId)의 추천질문만 업데이트
    public void updateSuggestQuestions(Long memberId, Long sessionId, Long chatId, List<String> followUps) {
      // 세션 소유권 검증
      sessionService.requireOwnedSession(memberId, sessionId);
      
      // chatId로 메시지 조회
      ChatMessage m = messageRepo.findById(chatId)
          .orElseThrow(() -> new NoSuchElementException("ChatMessage not found: " + chatId));
      
      if (followUps == null) followUps = List.of();
        m.setSuggestQ1(followUps.size() > 0 ? safeTrim(followUps.get(0)) : null);
        m.setSuggestQ2(followUps.size() > 1 ? safeTrim(followUps.get(1)) : null);
        m.setSuggestQ3(followUps.size() > 2 ? safeTrim(followUps.get(2)) : null);

      log.info("[ChatMessageService] updateSuggestQuestions ok | chatId={} qCount={}",
              chatId, Math.min(3, followUps.size()));
    }

    // 키워드 검색 + 날짜별 그룹핑
    @Transactional(readOnly = true)
    public List<ChatSessionDto.GroupedByDate<ChatSessionDto.SearchResultItem>> searchMyMessages(Long memberId, String keyword, int size) {
      // keyword trim / empty 처리  
      if (keyword == null) keyword = "";
      keyword = keyword.trim();
      if (keyword.isEmpty()) return List.of();
      
      // size 범위 제한 (1~200)
      int safeSize = Math.max(1, Math.min(size, 200));
      
      // repository native query로 검색
      List<ChatMessage> found = messageRepo.searchMyMessages(memberId, keyword, PageRequest.of(0, safeSize));

      Map<String, List<ChatSessionDto.SearchResultItem>> map = new LinkedHashMap<>();
      DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
      
      // 결과를 createdAt.toLacalDate() 기준으로 날짜별 그룹핑
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
      
      // ChatSessionDto.GroupedByDate<SearchResultItem> 형태로 반환
      List<ChatSessionDto.GroupedByDate<ChatSessionDto.SearchResultItem>> out = new ArrayList<>();
      for (var e : map.entrySet()) {  
        out.add(new ChatSessionDto.GroupedByDate<>(e.getKey(), e.getValue()));
      }

      log.info("[ChatMessageService] searchMyMessages ok | memberId={} keyword='{}' hits={} days={}",
              memberId, keyword, found.size(), out.size());

      return out;
    }
    
    /** 내부 유틸 */
    private String safeTrim(String s) {
      if (s == null) return null;
      s = s.trim();
      return s.isBlank() ? null : s;
    }
}
