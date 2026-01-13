package dev.jpa.team2.chatbot.domain.embeddingchunk;

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
    private Long fileId;  // 이 청크가 어느 원본에 속하는지 식별자

    @Lob
    @Column(name = "CHUNK_TEXT", nullable = false)
    private String chunkText; // 실제 청크 텍스트 (길 수 있어서 @Lob)

    @Lob
    @Column(name = "VECTOR_DATA", nullable = false)
    private String vectorData;  // 임베딩 벡터

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}
