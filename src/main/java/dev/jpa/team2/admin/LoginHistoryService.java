package dev.jpa.team2.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class LoginHistoryService {

  private final LoginHistoryRepository loginHistoryRepository;
  private final MemberRepository memberRepository;

  public LoginHistoryService(LoginHistoryRepository loginHistoryRepository,
                             MemberRepository memberRepository) {
    this.loginHistoryRepository = loginHistoryRepository;
    this.memberRepository = memberRepository;
  }

  @Transactional
  public void record(Long memberId, boolean success, HttpServletRequest request) {
    // MEMBER_ID NOT NULL 이라서 memberId 없으면 저장 불가
    if (memberId == null) return;

    Member member = memberRepository.findByMemberId(memberId);
    if (member == null) return;

    LoginHistory h = new LoginHistory();
    h.setMember(member);
    h.setSuccessYn(success ? "Y" : "N");
    h.setLoginIp(extractClientIp(request));
    h.setUserAgent(request.getHeader("User-Agent"));

    loginHistoryRepository.save(h);
  }

  private String extractClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
