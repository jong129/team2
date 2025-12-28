package dev.jpa.team2.chatbot;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
public class ChatDataRef {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "CHAT_DATA_REF_SEQ"
    )
    @SequenceGenerator(
        name = "CHAT_DATA_REF_SEQ",
        sequenceName = "SEQ_CHAT_DATA_REF_ID",
        allocationSize = 1
    )
    @Column(name = "REF_ID")
    private Long refId;

    @Column(name = "CHAT_ID", nullable = false)
    private Long chatId;

    @Column(name = "CHUNK_ID", nullable = false)
    private Long chunkId;

    @Column(name = "SIMILARITY_SCORE")
    private Double similarityScore;

    public ChatDataRef(
            Long chatId,
            Long chunkId,
            Double similarityScore) {

        this.chatId = chatId;
        this.chunkId = chunkId;
        this.similarityScore = similarityScore;
    }
}
