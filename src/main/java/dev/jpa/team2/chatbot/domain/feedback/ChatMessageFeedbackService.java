package dev.jpa.team2.chatbot.domain.feedback;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.domain.message.ChatMessage;
import dev.jpa.team2.chatbot.domain.message.ChatMessageRepository;
import lombok.RequiredArgsConstructor;

// 좋아요/싫어요 클릭을 규칙에 맞게 Insert/Update/Delete 하고, 동시에 CHAT_MESSAGE.likeCount/dislikeCount를 즉시 증감

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageFeedbackService {

    private final ChatMessageRepository chatMessageRepo;
    private final ChatMessageFeedbackRepository feedbackRepo;

    /**
     * 규칙
     * - ASSISTANT 메시지만 평가 가능
     * - (memberId, chatId) 피드백은 1개만 존재
     * - 같은 값 다시 누르면 취소(Delete)
     * - 다른 값 누르면 변경(Update)
     * - 변경과 동시에 CHAT_MESSAGE.like_count/dislike_count는 즉시 반영
     */
    
    public ChatMessageFeedbackDto upsertOrToggle(
        Long memberId,
        Long chatId,
        boolean liked
    ) {
        int newValue = liked ? 1 : -1;
        
        // 메시지 존재 확인
        ChatMessage msg = chatMessageRepo.findById(chatId)
            .orElseThrow(() -> new IllegalArgumentException("메시지가 없습니다. chatId=" + chatId));
        
        // msg.role이 ASSISTANT가 아니면 예외
        if (!"ASSISTANT".equalsIgnoreCase(msg.getRole())) {
            throw new IllegalStateException("AI 답변만 평가할 수 있습니다.");
        }
        
        // 기존 피드백 조회
        var opt = feedbackRepo.findByMemberIdAndChatId(memberId, chatId);

        Integer myFeedback = null;
        
        if (opt.isEmpty()) {  // 기존 피드백이 없으면 (첫 클릭)
            feedbackRepo.save(ChatMessageFeedback.of(chatId, memberId, newValue));  // Insert
            applyDelta(msg, newValue, +1);  // 호출
            myFeedback = newValue;
        } else {  // 기존 피드백이 있고, 같은 값을 또 누르면 (취소)
            ChatMessageFeedback exist = opt.get();
            int oldValue = exist.getValue();

            if (oldValue == newValue) {
                feedbackRepo.delete(exist); // 피드백 행 삭제
                applyDelta(msg, oldValue, -1);  // 해당 집계 -1
                myFeedback = null;
            } else {  // 기존 피드백이 있고, 반대로 누르면 (변경)
                exist.setValue(newValue);                       // Update
                exist.setUpdatedAt(LocalDateTime.now());  // Update
                applyDelta(msg, oldValue, -1);  // 기존 값 감소
                applyDelta(msg, newValue, +1);  // 새 값 증가
                myFeedback = newValue;
            }
        }

        return ChatMessageFeedbackDto.builder()
            .chatId(chatId)
            .likeCount(msg.getLikeCount())
            .dislikeCount(msg.getDislikeCount())
            .myFeedback(myFeedback)
            .build();
    }

    // 보조 메서드 : msg.getLikeCount()가 null이면 0 취급. 감소 결과가 음수 되지 않게 Math.max(0) 처리
    private void applyDelta(ChatMessage msg, int value, int delta) {
        if (value == 1) {
            msg.setLikeCount(Math.max(0, (msg.getLikeCount() == null ? 0 : msg.getLikeCount()) + delta));
        } else if (value == -1) {
            msg.setDislikeCount(Math.max(0, (msg.getDislikeCount() == null ? 0 : msg.getDislikeCount()) + delta));
        }
        // msg는 영속 상태라서 트랜잭션 종료 시 자동 flush
    }
}
