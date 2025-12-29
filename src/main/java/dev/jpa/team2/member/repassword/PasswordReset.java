package dev.jpa.team2.member.repassword;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PASSWORD_RESET")
@Getter
@Setter
@NoArgsConstructor
public class PasswordReset {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "password_reset_seq"
    )
    @SequenceGenerator(
        name = "password_reset_seq",
        sequenceName = "SEQ_PASSWORD_RESET_ID",
        allocationSize = 1
    )
    @Column(name = "RESET_ID")
    private Long resetId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @Column(name = "RESET_CODE", nullable = false, unique = true)
    private String resetCode;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "USED_YN", nullable = false)
    private String usedYn = "N";

    @Column(name = "USED_AT")
    private LocalDateTime usedAt;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
