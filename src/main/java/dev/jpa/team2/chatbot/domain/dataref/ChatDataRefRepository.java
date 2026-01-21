package dev.jpa.team2.chatbot.domain.dataref;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatDataRefRepository extends JpaRepository<ChatDataRef, Long> {
    // CHAT_DATA_REF 조회 저장 : 특정 회원의 특정 세션에 붙은 컨텍스트를 최신순으로 가져옴
    List<ChatDataRef> findByMemberIdAndSessionIdOrderByCreatedAtDesc(Long memberId, Long sessionId);
}
