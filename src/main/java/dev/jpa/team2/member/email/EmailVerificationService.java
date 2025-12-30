package dev.jpa.team2.member.email;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailSendService emailSendService;

    public EmailVerificationService(
            EmailVerificationRepository emailVerificationRepository,
            EmailSendService emailSendService) {

        this.emailVerificationRepository = emailVerificationRepository;
        this.emailSendService = emailSendService;
    }

    /* ===============================
       1) 인증코드 생성
    =============================== */
    private String generateVerifyCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6자리
        return String.valueOf(code);
    }

    /* ===============================
       2) 만료 시간 계산 (현재 + 5분)
    =============================== */
    private Date createExpireAt() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);
        return cal.getTime();
    }

    /* ===============================
       3-A) 회원가입용 인증 요청
       - 이미 인증된 이메일이면 예외
    =============================== */
    public void createForSignup(String email) {

        Optional<EmailVerification> optional =
                emailVerificationRepository.findByEmail(email);

        if (optional.isPresent() && "Y".equals(optional.get().getVerifiedYn())) {
            throw new IllegalStateException("이미 인증이 완료된 이메일입니다.");
        }

        EmailVerification ev = createOrUpdateInternal(email);

        // 🔥 회원가입용 메일 발송
        emailSendService.sendVerificationMail(email, ev.getVerifyCode());
    }

    /* ===============================
       3-B) 비밀번호 재설정용 인증 요청
       - 이미 인증된 이메일이어도 재발급 허용
    =============================== */
    public void createForPasswordReset(String email) {

        EmailVerification ev = createOrUpdateInternal(email);

        // 🔥 비밀번호 재설정용 메일 발송
        emailSendService.sendPasswordResetVerificationMail(
                email,
                ev.getVerifyCode()
        );
    }

    /* ===============================
       3-C) 내부 공통 로직
       - DB 처리만 담당
       - 메일 발송 ❌
    =============================== */
    private EmailVerification createOrUpdateInternal(String email) {

        Optional<EmailVerification> optional =
                emailVerificationRepository.findByEmail(email);

        String code = generateVerifyCode();
        Date expiresAt = createExpireAt();
        EmailVerification ev;

        if (optional.isPresent()) {
            ev = optional.get();
            ev.updateCode(code, expiresAt);
            ev.resetVerified(); // 인증 상태 초기화
        } else {
            ev = new EmailVerification(email, code, expiresAt);
        }

        emailVerificationRepository.save(ev);
        return ev;
    }

    /* ===============================
       4) 인증번호 검증
    =============================== */
    public void verify(String email, String verifyCode) {

        EmailVerification ev =
                emailVerificationRepository
                        .findByEmailAndVerifyCode(email, verifyCode)
                        .orElseThrow(() ->
                                new IllegalArgumentException("인증번호가 틀렸습니다.")
                        );

        if (ev.getExpiresAt().before(new Date())) {
            throw new IllegalStateException("인증번호가 만료되었습니다.");
        }

        ev.verify();
        emailVerificationRepository.save(ev);
    }

    /* ===============================
       5) 인증 완료 여부 확인 (회원가입용)
    =============================== */
    public boolean isVerified(String email) {
        return emailVerificationRepository.existsByEmailAndVerifiedYn(email, "Y");
    }
}
