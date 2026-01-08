package dev.jpa.team2.checklist.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * USER_PRE_PROFILE_KEY
 *
 * ✔ PRE 체크 결과 요약(JSON)
 * ✔ 동일 패턴 재사용을 위한 해시 키
 */
@Entity
@Table(
    name = "USER_PRE_PROFILE_KEY",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UQ_USER_PRE_PROFILE_KEY_HASH",
            columnNames = {"KEY_HASH"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreProfileKey {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "SEQ_USER_PRE_PROFILE_KEY_ID"
    )
    @SequenceGenerator(
        name = "SEQ_USER_PRE_PROFILE_KEY_ID",
        sequenceName = "SEQ_USER_PRE_PROFILE_KEY_ID",
        allocationSize = 1
    )
    @Column(name = "PROFILE_KEY_ID")
    private Long profileKeyId;

    @Column(name = "KEY_HASH", nullable = false, length = 64)
    private String keyHash;

    @Lob
    @Column(name = "KEY_JSON", nullable = false)
    private String keyJson;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
