package dev.jpa.team2.chatbot.ragblocked;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRagBlockedChunkDto {

    // 요청
    private Long chunkId;
    private String reason;

    // 응답
    private Long blockId;
    private Integer isActive;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static ChatRagBlockedChunkDto from(ChatRagBlockedChunk e) {
        return ChatRagBlockedChunkDto.builder()
            .blockId(e.getBlockId())
            .chunkId(e.getChunkId())
            .reason(e.getReason())
            .isActive(e.getIsActive())
            .createdBy(e.getCreatedBy())
            .createdAt(e.getCreatedAt())
            .build();
    }
}
