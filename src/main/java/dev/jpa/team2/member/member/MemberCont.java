package dev.jpa.team2.member.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import jakarta.servlet.http.HttpServletRequest;
import dev.jpa.team2.admin.LoginHistoryService;

import dev.jpa.team2.member.member_role.MemberRoleRepository;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/member")
public class MemberCont {

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private MemberService memberService;
  
  @Autowired
  private MemberRoleRepository memberRoleRepository;

  @Autowired
  private LoginHistoryService loginHistoryService;

  public MemberCont() {
    System.out.println("-> MemberController created.");
  }

  /**
   * 회원 등록
   */
  @PostMapping(path = "/save")
  public ResponseEntity<Map<String, Object>> save(@RequestBody MemberDTO memberDTO) {

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
   */
  @GetMapping(path = "/check_login_id")
  public ResponseEntity<Integer> checkLoginId(@RequestParam(name = "loginId", defaultValue = "") String loginId) {

    Integer cnt = memberService.checkLoginId(loginId);
    return ResponseEntity.ok(cnt);
  }

  /**
   * 전체 목록
   */
  @GetMapping(path = "/find_all")
  public ResponseEntity<List<Member>> findAll() {
    return ResponseEntity.ok(memberService.findAllByOrderByMemberIdAsc());
  }

  /**
   * 회원 조회
   */
  @GetMapping(path = "/read/{memberId}")
  public ResponseEntity<Member> findByMemberId(@PathVariable("memberId") Long memberId) {

    Member member = memberService.findByMemberId(memberId);
    return ResponseEntity.ok(member);
  }

  /**
   * 회원 정보 수정
   */
  @PutMapping(path = "/update")
  public ResponseEntity<Integer> update(@RequestBody MemberDTO memberDTO) {
    int cnt = memberService.update(memberDTO);
    return ResponseEntity.ok(cnt);
  }

  /**
   * 비밀번호 변경
   */
  @PostMapping(path = "/update_password")
  public ResponseEntity<Integer> updatePassword(@RequestBody MemberDTO memberDTO) {

    int cnt = memberService.updatePassword(memberDTO.getMemberId(), memberDTO.getPassword());

    return ResponseEntity.ok(cnt);
  }

  /**
   * 회원 삭제
   */
  @DeleteMapping(path = "/delete/{memberId}")
  public ResponseEntity<Integer> delete(@PathVariable("memberId") Long memberId) {

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
   */
  @PostMapping(path = "/login")
  public ResponseEntity<Map<String, Object>> login(
      @RequestParam(name = "loginInput", defaultValue = "") String loginInput,
      @RequestParam(name = "password", defaultValue = "") String password, HttpSession session,
      HttpServletRequest request // ✅ 추가
  ) {

    Map<String, Object> map = new HashMap<>();

    Member member = memberService.loginTarget(loginInput);

    if (member == null) {
      map.put("cnt", 0); // 아이디/이메일 없음
      // ❗ MEMBER_ID가 없어서 LOGIN_HISTORY 저장 불가 (FK + NOT NULL)
      return ResponseEntity.ok(map);
    }

    // 비밀번호 불일치
    if (!passwordEncoder.matches(password, member.getPassword())) {
      memberService.loginFail(member.getMemberId());

      // ✅ 실패 로그 저장
      loginHistoryService.record(member.getMemberId(), false, request);

      map.put("cnt", 2);
      return ResponseEntity.ok(map);
    }

    // 계정 잠금
    if ("LOCKED".equals(member.getStatus())) {

      // ✅ 잠금 상태도 로그인 실패로 기록(원하면 빼도 됨)
      loginHistoryService.record(member.getMemberId(), false, request);

      map.put("cnt", 3);
      return ResponseEntity.ok(map);
    }

    // 로그인 성공
    memberService.loginSuccess(member.getMemberId());

    session.setAttribute("LOGIN_MEMBER_ID", member.getMemberId());
    
    // roles 조회
    List<String> roles = memberRoleRepository.findRoleNamesByMemberId(member.getMemberId());

    // 세션에도 저장(백엔드 관리자 API 보호용)
    session.setAttribute("LOGIN_ROLES", roles);  
    map.put("cnt", 1); // 로그인 성공

    // ✅ 성공 로그 저장
    loginHistoryService.record(member.getMemberId(), true, request);
    map.put("cnt", 1);
    map.put("memberId", member.getMemberId());
    map.put("loginId", member.getLoginId());
    map.put("status", member.getStatus());
    map.put("name", member.getName());

    return ResponseEntity.ok(map);
  }

  /**
   * 아이디 찾기
   */
  @PostMapping(path = "/find_id")
  public ResponseEntity<Map<String, Object>> findId(@RequestParam(name = "name") String name,
      @RequestParam(name = "email") String email) {

    Map<String, Object> map = new HashMap<>();

    try {
      String maskedLoginId = memberService.findLoginIdByNameAndEmail(name, email);

      map.put("success", true);
      map.put("loginId", maskedLoginId);

    } catch (IllegalArgumentException e) {
      map.put("success", false);
      map.put("message", e.getMessage());
    }

    return ResponseEntity.ok(map);
  }

  /**
   * 이름 또는 이메일 검색
   */
  @GetMapping(path = "/search")
  public ResponseEntity<List<Member>> search(@RequestParam(name = "keyword", defaultValue = "") String keyword) {

    return ResponseEntity.ok(memberService.search(keyword));
  }
}
