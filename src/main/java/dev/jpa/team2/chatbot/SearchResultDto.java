package dev.jpa.team2.chatbot;

import java.time.LocalDateTime;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private Long sessionId;
    private Long messageId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
