package dev.jpa.team2.member.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)

@RestController
@RequestMapping("/member")
public class MemberCont {

  @Autowired
  private MemberService memberService;

  public MemberCont() {
    System.out.println("-> MemberController created.");
  }

  /**
   * 회원 등록
   * http://localhost:9100/member/save
   */
  @PostMapping(path = "/save")
  public ResponseEntity<Map<String, Object>> save(
      @RequestBody MemberDTO memberDTO) {

    Map<String, Object> result = new HashMap<>();

    try {
      Member savedEntity = memberService.save(memberDTO);
      result.put("success", true);
      result.put("memberId", savedEntity.getMemberId());
    } catch (IllegalStateException e) {
      result.put("success", false);
      result.put("message", e.getMessage());
    }

    return ResponseEntity.ok(result);
  }

  /**
   * 로그인 ID 중복 검사
   * http://localhost:9100/member/check_login_id?loginId=user1
   */
  @GetMapping(path = "/check_login_id")
  public ResponseEntity<Integer> checkLoginId(
      @RequestParam(name = "loginId", defaultValue = "") String loginId) {

    Integer cnt = memberService.checkLoginId(loginId);
    return ResponseEntity.ok(cnt);
  }

  /**
   * 전체 목록
   * http://localhost:9100/member/find_all
   */
  @GetMapping(path = "/find_all")
  public ResponseEntity<List<Member>> findAll() {
    return ResponseEntity.ok(memberService.findAllByOrderByMemberIdAsc());
  }

  /**
   * 회원 조회
   * http://localhost:9100/member/read/3
   */
  @GetMapping(path = "/read/{memberId}")
  public ResponseEntity<Member> findByMemberId(
      @PathVariable("memberId") Long memberId) {

    Member member = memberService.findByMemberId(memberId);
    return ResponseEntity.ok(member);
  }

  /**
   * 회원 정보 수정
   * http://localhost:9100/member/update
   */
  @PutMapping(path = "/update")
  public ResponseEntity<Integer> update(@RequestBody MemberDTO memberDTO) {
    int cnt = memberService.update(memberDTO);
    return ResponseEntity.ok(cnt);
  }

  /**
   * 비밀번호 변경
   * http://localhost:9100/member/update_password
   */
  @PostMapping(path = "/update_password")
  public ResponseEntity<Integer> updatePassword(@RequestBody MemberDTO memberDTO) {

    int cnt = memberService.updatePassword(
        memberDTO.getMemberId(),
        memberDTO.getPassword()
    );

    return ResponseEntity.ok(cnt);
  }

  /**
   * 회원 삭제
   * http://localhost:9100/member/delete/3
   */
  @DeleteMapping(path = "/delete/{memberId}")
  public ResponseEntity<Integer> delete(
      @PathVariable("memberId") Long memberId) {

    Optional<Member> member = memberService.findById(memberId);
    int cnt = 0;

    if (member.isPresent()) {
      memberService.delete(memberId);
      cnt = 1;
    } else {
      cnt = 2; // Not Found
    }

    return ResponseEntity.ok(cnt);
  }

  /**
   * 로그인
   * http://localhost:9100/member/login?loginInput=user1&password=1234
   */
  @PostMapping(path = "/login")
  public ResponseEntity<Map<String, Object>> login(
      @RequestParam(name = "loginInput", defaultValue = "") String loginInput,
      @RequestParam(name = "password", defaultValue = "") String password) {

    Map<String, Object> map = new HashMap<>();

    Member member = memberService.loginTarget(loginInput);

    if (member == null) {
      map.put("cnt", 0); // 아이디/이메일 없음
      return ResponseEntity.ok(map);
    }

    if (!member.getPassword().equals(password)) {
      memberService.loginFail(member.getMemberId());
      map.put("cnt", 2); // 비밀번호 불일치
      return ResponseEntity.ok(map);
    }

    if ("LOCKED".equals(member.getStatus())) {
      map.put("cnt", 3); // 계정 잠금
      return ResponseEntity.ok(map);
    }

    memberService.loginSuccess(member.getMemberId());

    map.put("cnt", 1); // 로그인 성공
    map.put("memberId", member.getMemberId());
    map.put("loginId", member.getLoginId());
    map.put("status", member.getStatus());

    return ResponseEntity.ok(map);
  }

  /**
   * 이름 또는 이메일 검색
   * http://localhost:9100/member/search?keyword=홍길동
   */
  @GetMapping(path = "/search")
  public ResponseEntity<List<Member>> search(
      @RequestParam(name = "keyword", defaultValue = "") String keyword) {

    return ResponseEntity.ok(memberService.search(keyword));
  }
}
