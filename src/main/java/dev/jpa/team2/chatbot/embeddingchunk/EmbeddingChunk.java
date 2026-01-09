package dev.jpa.team2.chatbot.embeddingchunk;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class EmbeddingChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EMBEDDING_CHUNK_SEQ")
    @SequenceGenerator(name = "EMBEDDING_CHUNK_SEQ", sequenceName = "SEQ_CHUNK_ID", allocationSize = 1)
    @Column(name = "CHUNK_ID")
    private Long chunkId;

    @Column(name = "FILE_ID", nullable = false)
    private Long fileId;

    @Lob
    @Column(name = "CHUNK_TEXT", nullable = false)
    private String chunkText;

    @Lob
    @Column(name = "VECTOR_DATA", nullable = false)
    private String vectorData;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}
