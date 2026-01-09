package dev.jpa.team2.member.email;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.member.member.Member;

public interface EmailVerificationRepository
        extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmail(String email);

    Optional<EmailVerification> findByEmailAndVerifyCode(
        String email,
        String verifyCode
    );

    boolean existsByEmailAndVerifiedYn(String email, String verifiedYn);

    Optional<EmailVerification> findByEmailAndLoginId(
        String email,
        String loginId
    );
}
