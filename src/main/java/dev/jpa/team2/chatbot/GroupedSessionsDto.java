package dev.jpa.team2.chatbot;

import java.util.List;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupedSessionsDto {
    private String date; // "YYYY-MM-DD"
    private List<ChatSessionDto> sessions;
}
