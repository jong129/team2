package dev.jpa.team2.member.mypage;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import dev.jpa.team2.admin_inquiries_reply.AdminInquiryReply;
import dev.jpa.team2.admin_inquiries_reply.AdminInquiryReplyRepository;

import dev.jpa.team2.member.email.EmailVerificationService;
import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;
import dev.jpa.team2.member.repassword.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class MyPageService {

  private final MemberRepository memberRepository;
  private final EmailVerificationService emailVerificationService;
  private final PasswordResetService passwordResetService;
  private final MyPageLogWriter logWriter;

  private final UserInquiryRepository userInquiryRepository;
  private final AdminInquiryReplyRepository adminInquiryReplyRepository; // ✅ 추가(답변 조회)

  public MyPageService(
      MemberRepository memberRepository,
      EmailVerificationService emailVerificationService,
      PasswordResetService passwordResetService,
      MyPageLogWriter logWriter,
      UserInquiryRepository userInquiryRepository,
      AdminInquiryReplyRepository adminInquiryReplyRepository // ✅ 추가
  ) {
    this.memberRepository = memberRepository;
    this.emailVerificationService = emailVerificationService;
    this.passwordResetService = passwordResetService;
    this.logWriter = logWriter;
    this.userInquiryRepository = userInquiryRepository;
    this.adminInquiryReplyRepository = adminInquiryReplyRepository; // ✅ 추가
  }

  private Long requireLoginMemberId(HttpSession session) {
    Object v = session.getAttribute("LOGIN_MEMBER_ID");
    if (v == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    if (v instanceof Long) return (Long) v;
    return Long.valueOf(String.valueOf(v));
  }

  private Member requireUsableMember(Long memberId) {
    Member m = memberRepository.findByMemberId(memberId);
    if (m == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 정보가 존재하지 않습니다.");
    if ("WITHDRAWN".equals(m.getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "탈퇴 처리된 계정입니다.");
    }
    return m;
  }

  public MyPageMeResDto me(HttpSession session) {
    Long memberId = requireLoginMemberId(session);
    Member m = requireUsableMember(memberId);

    return new MyPageMeResDto(
        m.getMemberId(),
        m.getLoginId(),
        m.getEmail(),
        m.getName(),
        m.getPhone(),
        m.getStatus(),
        m.getCreatedAt(),
        m.getUpdatedAt()
    );
  }

  public void updateName(HttpSession session, MyPageUpdateNameReqDto dto, HttpServletRequest request) {
    Long memberId = requireLoginMemberId(session);
    Member m = requireUsableMember(memberId);

    String newName = dto.getName() == null ? "" : dto.getName().trim();
    if (newName.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이름은 필수입니다.");

    String oldName = m.getName();
    if (oldName != null && oldName.equals(newName)) return;

    int cnt = memberRepository.updateName(memberId, newName);

    System.out.println(">>> updateName cnt=" + cnt + ", memberId=" + memberId + ", newName=" + newName);

    if (cnt != 1) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이름 변경 실패");

    logWriter.onNameChanged(memberId, oldName, newName, request);
  }

  /**
   * 비번 변경 Step1: 인증번호 발송 (마이페이지는 로그인 상태라 loginId/email을 DB에서 가져와 고정)
   */
  public void sendPasswordVerifyCode(HttpSession session) {
    Long memberId = requireLoginMemberId(session);
    Member m = requireUsableMember(memberId);

    emailVerificationService.createForPasswordReset(m.getLoginId(), m.getEmail());
  }

  /**
   * 비번 변경 Step2: 인증번호 검증 + 비번 변경
   */
  public void changePassword(HttpSession session, MyPagePasswordChangeReqDto dto, HttpServletRequest request) {
    Long memberId = requireLoginMemberId(session);
    Member m = requireUsableMember(memberId);

    String code = dto.getVerifyCode() == null ? "" : dto.getVerifyCode().trim();
    if (code.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증번호가 필요합니다.");

    // 1) 이메일 인증번호 검증
    emailVerificationService.verify(m.getEmail(), code);

    // 2) reset 토큰 발급
    String resetCode = passwordResetService.createResetToken(m.getLoginId(), m.getEmail());

    // 3) 비밀번호 변경
    passwordResetService.resetPassword(resetCode, dto.getNewPassword(), dto.getConfirmPassword());

    logWriter.onPasswordChanged(memberId, request);
  }

  public void withdraw(HttpSession session, String reason, HttpServletRequest request) {
    Long memberId = requireLoginMemberId(session);
    requireUsableMember(memberId);

    int cnt = memberRepository.withdraw(memberId);
    if (cnt != 1) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "회원탈퇴 처리 실패");

    logWriter.onWithdrawn(memberId, reason, request);
  }

  // =========================
  // 문의
  // =========================

  public Long createInquiry(Long memberId, UserInquiryCreateRequest req) {
    if (memberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    requireUsableMember(memberId);

    if (req == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청이 올바르지 않습니다.");

    String title = req.getTitle() == null ? "" : req.getTitle().trim();
    String content = req.getContent() == null ? "" : req.getContent().trim();
    if (title.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "제목은 필수입니다.");
    if (content.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "내용은 필수입니다.");

    UserInquiry e = new UserInquiry();
    e.setMemberId(memberId);
    e.setTitle(title);
    e.setContent(req.getContent()); // 줄바꿈 유지
    e.setCategory(req.getCategory() == null ? null : req.getCategory().trim());
    e.setStatus("RECEIVED");

    return userInquiryRepository.save(e).getInquiryId();
  }

  // 내 문의 목록
  @Transactional(readOnly = true)
  public Page<UserInquiryRowDto> myInquiryList(Long memberId, Pageable pageable) {
    if (memberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    requireUsableMember(memberId);

    return userInquiryRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
        .map(x -> new UserInquiryRowDto(
            x.getInquiryId(),
            x.getTitle(),
            x.getCategory(),
            x.getStatus(),
            x.getCreatedAt()
        ));
  }

  // 내 문의 상세 (✅ 답변 포함)
  @Transactional(readOnly = true)
  public UserInquiryDetailDto myInquiryDetail(Long memberId, Long inquiryId) {
    if (memberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    requireUsableMember(memberId);

    UserInquiry x = userInquiryRepository.findByInquiryIdAndMemberId(inquiryId, memberId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문의가 존재하지 않습니다."));

    AdminInquiryReply r = adminInquiryReplyRepository.findByInquiryId(inquiryId).orElse(null);

    return new UserInquiryDetailDto(
        x.getInquiryId(),
        x.getMemberId(),
        x.getTitle(),
        x.getContent(),
        x.getCategory(),
        x.getStatus(),
        x.getCreatedAt(),
        r == null ? null : r.getReplyId(),
        r == null ? null : r.getContent(),
        r == null ? null : r.getAnsweredAt()
    );
  }
}
