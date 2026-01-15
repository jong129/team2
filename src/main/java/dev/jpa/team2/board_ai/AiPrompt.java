package dev.jpa.team2.board_ai;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "AI_PROMPT")
@Getter
@Setter
public class AiPrompt {

    @Id
    @Column(name = "PROMPT_CODE", length = 50)
    private String promptCode;

    @Column(name = "AI_TYPE", nullable = false, length = 30)
    private String aiType; // SUMMARY / SENTIMENT

    @Lob
    @Column(name = "PROMPT_TEXT", nullable = false)
    private String promptText;

    @Column(name = "VERSION", length = 20)
    private String version;

    @Column(name = "USE_YN", nullable = false, length = 1)
    private String useYn = "Y";

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
