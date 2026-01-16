package dev.jpa.team2.board_ai;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Entity
@Table(name = "BOARD_AI_DRAFT")
@Getter
@Setter
public class BoardAiDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BOARD_AI_DRAFT_ID_GEN")
    @SequenceGenerator(
            name = "SEQ_BOARD_AI_DRAFT_ID_GEN",
            sequenceName = "SEQ_BOARD_AI_DRAFT_ID",
            allocationSize = 1
    )
    @Column(name = "DRAFT_ID")
    private Long draftId;

    @Column(name = "CATEGORY_ID", nullable = false)
    private Long categoryId;

    @Column(name = "AI_TYPE", nullable = false, length = 30)
    private String aiType; // WRITE 고정으로 들어갈 예정

    @Column(name = "INPUT_TITLE", length = 2000)
    private String inputTitle;

    @Lob
    @Column(name = "INPUT_CONTENT")
    private String inputContent;

    @Column(name = "INPUT_HASH", nullable = false, length = 64)
    private String inputHash; // title+content 해시(캐시 키)

    @Lob
    @Column(name = "AI_RESULT", nullable = false)
    private String aiResult;

    @Column(name = "PROMPT_CODE", nullable = false, length = 50)
    private String promptCode;

    @Column(name = "MODEL_NAME", nullable = false, length = 50)
    private String modelName;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (modelName == null || modelName.isBlank()) modelName = "unknown";
        if (aiType == null || aiType.isBlank()) aiType = AiType.WRITE.name();
        if (inputHash == null || inputHash.isBlank()) inputHash = makeInputHash(inputTitle, inputContent);
    }

    // ServiceImpl에서 호출하는 정적 메서드 이름 그대로 맞춤
    public static String makeInputHash(String title, String content) {
        String t = title == null ? "" : title.trim();
        String c = content == null ? "" : content.trim();
        String raw = t + "\n" + c;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return toHex(dig);
        } catch (Exception e) {
            // 해시 실패 시에도 캐시 키가 필요하니 fallback
            return String.valueOf(raw.hashCode());
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
