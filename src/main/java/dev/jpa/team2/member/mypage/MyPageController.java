package dev.jpa.team2.member.mypage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/mypage")
public class MyPageController {

  private final MyPageService myPageService;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public MyPageController(MyPageService myPageService,
                          MemberRepository memberRepository,
                          PasswordEncoder passwordEncoder) {
    this.myPageService = myPageService;
    this.memberRepository = memberRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(HttpSession session) {
    return ResponseEntity.ok(myPageService.me(session));
  }

  @PutMapping("/profile/name")
  public ResponseEntity<?> updateName(
      @RequestBody MyPageUpdateNameReqDto dto,
      HttpSession session,
      HttpServletRequest request
  ) {
    myPageService.updateName(session, dto, request);
    return ResponseEntity.ok(Map.of("success", true, "message", "이름이 변경되었습니다."));
  }

  @PostMapping("/password/send")
  public ResponseEntity<?> sendPasswordCode(HttpSession session) {
    myPageService.sendPasswordVerifyCode(session);
    return ResponseEntity.ok(Map.of("success", true, "message", "인증번호가 이메일로 발송되었습니다."));
  }

  @PostMapping("/password/change")
  public ResponseEntity<?> changePassword(
      @RequestBody MyPagePasswordChangeReqDto dto,
      HttpSession session,
      HttpServletRequest request
  ) {
    myPageService.changePassword(session, dto, request);

    // 비밀번호 변경 성공 시 즉시 로그아웃(세션 만료)
    session.invalidate();

    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "비밀번호가 변경되었습니다. 다시 로그인해주세요."
    ));
  }


  @PostMapping("/withdraw")
  public ResponseEntity<?> withdraw(
      @RequestBody MyPageWithdrawReqDto dto,
      HttpSession session,
      HttpServletRequest request
  ) {
    Map<String, Object> res = new HashMap<>();

    Object v = session.getAttribute("LOGIN_MEMBER_ID");
    if (v == null) {
      res.put("success", false);
      res.put("message", "로그인이 필요합니다.");
      return ResponseEntity.status(401).body(res);
    }

    Long memberId = (Long) v;
    Member member = memberRepository.findByMemberId(memberId);
    if (member == null) {
      res.put("success", false);
      res.put("message", "회원 정보가 존재하지 않습니다.");
      return ResponseEntity.status(404).body(res);
    }

    if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
      res.put("success", false);
      res.put("message", "비밀번호를 입력해야 합니다.");
      return ResponseEntity.badRequest().body(res);
    }

    if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
      res.put("success", false);
      res.put("message", "비밀번호가 일치하지 않습니다.");
      return ResponseEntity.badRequest().body(res);
    }

    myPageService.withdraw(session, dto.getReason(), request);

    session.invalidate();

    res.put("success", true);
    res.put("message", "회원탈퇴가 완료되었습니다.");
    return ResponseEntity.ok(res);
  }
}
