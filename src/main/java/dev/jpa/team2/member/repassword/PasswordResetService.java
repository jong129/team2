package dev.jpa.team2.member.repassword;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;
import dev.jpa.team2.member.email.EmailVerificationRepository;

import dev.jpa.team2.member.repassword.PasswordReset;
import dev.jpa.team2.member.repassword.PasswordResetRepository;


@Service
@Transactional
public class PasswordResetService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            EmailVerificationRepository emailVerificationRepository,
            MemberRepository memberRepository,
            PasswordResetRepository passwordResetRepository,
            PasswordEncoder passwordEncoder) {

        this.emailVerificationRepository = emailVerificationRepository;
        this.memberRepository = memberRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 이메일 인증 성공 후
     * - PasswordReset 토큰 발급
     */
    public String createResetToken(String loginId, String email) {

        emailVerificationRepository
                .findByEmailAndLoginId(email, loginId)
                .filter(ev -> "Y".equals(ev.getVerifiedYn()))
                .orElseThrow(() ->
                        new IllegalStateException("이메일 인증이 완료되지 않았습니다.")
                );

        Member member =
                memberRepository.findByLoginId(loginId)
                        .orElseThrow(() ->
                                new IllegalStateException("회원 정보 없음")
                        );

        PasswordReset reset = new PasswordReset();
        reset.setMemberId(member.getMemberId());
        reset.setResetCode(UUID.randomUUID().toString());
        reset.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        passwordResetRepository.save(reset);
        return reset.getResetCode();
    }

    /**
     * 비밀번호 변경
     */
    public void resetPassword(
            String resetCode,
            String newPassword,
            String confirmPassword) {

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        PasswordReset reset =
                passwordResetRepository.findByResetCode(resetCode)
                        .orElseThrow(() ->
                                new IllegalArgumentException("유효하지 않은 재설정 코드입니다.")
                        );

        if ("Y".equals(reset.getUsedYn())) {
            throw new IllegalStateException("이미 사용된 코드입니다.");
        }

        if (reset.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("만료된 코드입니다.");
        }

        Member member =
                memberRepository.findById(reset.getMemberId())
                        .orElseThrow(() ->
                                new IllegalStateException("회원 정보가 존재하지 않습니다.")
                        );

        member.setPassword(passwordEncoder.encode(newPassword));

        reset.setUsedYn("Y");
        reset.setUsedAt(LocalDateTime.now());
    }
}
