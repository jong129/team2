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

  public EmailVerificationService(EmailVerificationRepository emailVerificationRepository,
      EmailSendService emailSendService) {

    this.emailVerificationRepository = emailVerificationRepository;
    this.emailSendService = emailSendService;
  }

  /*
   * =============================== 1) 인증코드 생성 ===============================
   */
  private String generateVerifyCode() {
    Random random = new Random();
    int code = 100000 + random.nextInt(900000); // 6자리
    return String.valueOf(code);
  }

  /*
   * =============================== 2) 만료 시간 계산 (현재 + 5분)
   * ===============================
   */
  private Date createExpireAt() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MINUTE, 5);
    return cal.getTime();
  }

  /*
   * =============================== 3) 인증 요청 생성 / 재발급
   * ===============================
   */
  public void createOrUpdate(String email) {

    Optional<EmailVerification> optional = emailVerificationRepository.findByEmail(email);

    // 이미 인증 완료된 이메일
    if (optional.isPresent() && "Y".equals(optional.get().getVerifiedYn())) {
      throw new IllegalStateException("이미 인증이 완료된 이메일입니다.");
    }

    String code = generateVerifyCode();
    Date expiresAt = createExpireAt();
    EmailVerification ev;

    if (optional.isPresent()) {
      // 기존 요청 재발급 (UPDATE)
      ev = optional.get();
      ev.updateCode(code, expiresAt);
    } else {
      // 신규 요청 (INSERT)
      ev = new EmailVerification(email, code, expiresAt);
    }

    emailVerificationRepository.save(ev);

    // 🔥 인증 메일 발송
    emailSendService.sendVerificationMail(email, code);
  }

  /*
   * =============================== 4) 인증번호 검증 ===============================
   */
  public void verify(String email, String verifyCode) {

    // 1️⃣ 인증번호 자체가 틀린 경우
    EmailVerification ev =
        emailVerificationRepository
            .findByEmailAndVerifyCode(email, verifyCode)
            .orElseThrow(() ->
                new IllegalArgumentException("인증번호가 틀렸습니다.")
            );

    // 2️⃣ 만료된 경우
    if (ev.getExpiresAt().before(new Date())) {
        throw new IllegalStateException("인증번호가 만료되었습니다.");
    }

    // 3️⃣ 정상 인증
    ev.verify();
    emailVerificationRepository.save(ev);
}


  /*
   * =============================== 5) 인증 완료 여부 확인 (회원가입용)
   * ===============================
   */
  public boolean isVerified(String email) {
    return emailVerificationRepository.existsByEmailAndVerifiedYn(email, "Y");
  }
}
