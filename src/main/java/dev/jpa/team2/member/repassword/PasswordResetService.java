package dev.jpa.team2.member.repassword;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.admin.password.PasswordChangeHistoryService;
import dev.jpa.team2.member.email.EmailVerificationRepository;
import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;

@Service
@Transactional
public class PasswordResetService {

  private final EmailVerificationRepository emailVerificationRepository;
  private final MemberRepository memberRepository;
  private final PasswordResetRepository passwordResetRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordChangeHistoryService passwordChangeHistoryService;

  public PasswordResetService(
      EmailVerificationRepository emailVerificationRepository,
      MemberRepository memberRepository,
      PasswordResetRepository passwordResetRepository,
      PasswordChangeHistoryService passwordChangeHistoryService,
      PasswordEncoder passwordEncoder
  ) {
    this.emailVerificationRepository = emailVerificationRepository;
    this.memberRepository = memberRepository;
    this.passwordResetRepository = passwordResetRepository;
    this.passwordEncoder = passwordEncoder;
    this.passwordChangeHistoryService = passwordChangeHistoryService;
  }

  /**
   * 이메일 인증 성공 후 - PasswordReset 토큰 발급
   */
  public String createResetToken(String loginId, String email) {

    emailVerificationRepository
        .findByEmailAndLoginId(email, loginId)
        .filter(ev -> "Y".equals(ev.getVerifiedYn()))
        .orElseThrow(() -> new IllegalStateException("이메일 인증이 완료되지 않았습니다."));

    Member member = memberRepository
        .findByLoginId(loginId)
        .orElseThrow(() -> new IllegalStateException("회원 정보 없음"));

    PasswordReset reset = new PasswordReset();
    reset.setMemberId(member.getMemberId());
    reset.setResetCode(UUID.randomUUID().toString());
    reset.setExpiresAt(LocalDateTime.now().plusMinutes(10));

    passwordResetRepository.save(reset);
    return reset.getResetCode();
  }

  /**
   * 내부 공통: 실제 비밀번호 변경 수행 (로그는 여기서 찍지 않음)
   */
  private Member doReset(String resetCode, String newPassword, String confirmPassword) {

    if (!newPassword.equals(confirmPassword)) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    PasswordReset reset = passwordResetRepository.findByResetCode(resetCode)
        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 재설정 코드입니다."));

    if ("Y".equals(reset.getUsedYn())) {
      throw new IllegalStateException("이미 사용된 코드입니다.");
    }

    if (reset.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new IllegalStateException("만료된 코드입니다.");
    }

    Member member = memberRepository.findById(reset.getMemberId())
        .orElseThrow(() -> new IllegalStateException("회원 정보가 존재하지 않습니다."));

    // ✅ 비밀번호 실제 변경
    member.setPassword(passwordEncoder.encode(newPassword));

    // ✅ 토큰 사용 처리
    reset.setUsedYn("Y");
    reset.setUsedAt(LocalDateTime.now());

    return member;
  }

  /**
   * (마이페이지 등) 비밀번호 변경
   * - 마이페이지는 MyPageLogWriter에서 USER_CHANGE 로그를 남기므로
   *   여기서는 로그를 남기지 않는다. (중복 방지)
   */
  public void resetPassword(String resetCode, String newPassword, String confirmPassword) {
    doReset(resetCode, newPassword, confirmPassword);
  }

  /**
   * (비번찾기 전용) 비밀번호 변경 + 비번변경 이력 기록
   */
  public void resetPasswordWithHistory(String resetCode, String newPassword, String confirmPassword) {
    Member member = doReset(resetCode, newPassword, confirmPassword);

    passwordChangeHistoryService.record(
        member.getMemberId(),
        member.getMemberId(),   // 로그인 없이도 '본인 변경'으로 기록
        "RESET_CHANGE",
        null
    );
  }
}
