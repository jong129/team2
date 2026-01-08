// RagAskReq.java
package dev.jpa.team2.chatbot.rag;

import lombok.Data;

@Data
public class RagAskReq {
    private Long sessionId;
    private String question;
}
