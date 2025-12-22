package dev.jpa.team2.member.member;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberService {

  @Autowired
  MemberRepository memberRepository;

  public MemberService() {
    System.out.println("-> MemberService created");
  }

  /* ==================================================
   * 1) 중복 검사
   * ================================================== */

  /** 로그인 ID 중복 검사 */
  public int checkLoginId(String loginId) {
    return memberRepository.countByLoginId(loginId);
  }

  /** 이메일 중복 검사 */
  public int checkEmail(String email) {
    return memberRepository.countByEmail(email);
  }

  /* ==================================================
   * 2) 회원 등록
   * ================================================== */

  public Member save(MemberDTO memberDTO) {
    Member savedEntity = memberRepository.save(memberDTO.toEntity());
    System.out.println("-> memberId: " + savedEntity.getMemberId());
    return savedEntity;
  }

  /* ==================================================
   * 3) 전체 목록
   * ================================================== */

  public List<Member> findAllByOrderByMemberIdAsc() {
    return memberRepository.findAllByOrderByMemberIdAsc();
  }

  /* ==================================================
   * 4) 조회
   * ================================================== */

  /** PK 조회 */
  public Member findByMemberId(long memberId) {
    return memberRepository.findByMemberId(memberId);
  }

  /** 로그인 ID 조회 */
  public Member findByLoginId(String loginId) {
    return memberRepository.findByLoginId(loginId);
  }

  /** Optional PK 조회 */
  public Optional<Member> findById(long memberId) {
    return memberRepository.findById(memberId);
  }

  /* ==================================================
   * 5) 로그인 처리
   * ================================================== */

  /**
   * 로그인 대상 조회 (LOGIN_ID or EMAIL)
   */
  public Member loginTarget(String loginInput) {
    return memberRepository.findByLoginIdOrEmail(loginInput);
  }

  /**
   * 로그인 성공 처리
   */
  public int loginSuccess(Long memberId) {
    return memberRepository.loginSuccess(memberId);
  }

  /**
   * 로그인 실패 처리
   */
  public int loginFail(Long memberId) {
    int cnt = memberRepository.increaseFailCount(memberId);

    // 실패 횟수 5회 초과 시 계정 잠금
    Member member = memberRepository.findByMemberId(memberId);
    if (member.getFailedLoginCount() >= 5) {
      memberRepository.lockMember(memberId);
    }

    return cnt;
  }

  /* ==================================================
   * 6) 비밀번호 변경
   * ================================================== */

  public int updatePassword(Long memberId, String password) {
    int cnt = memberRepository.updatePassword(memberId, password);
    System.out.println("-> MemberService updatePassword cnt: " + cnt);
    return cnt;
  }

  /* ==================================================
   * 7) 회원 정보 수정
   * ================================================== */

  public int update(MemberDTO memberDTO) {
    return memberRepository.updateProfile(
        memberDTO.getName(),
        memberDTO.getPhone(),
        memberDTO.getMemberId()
    );
  }

  /* ==================================================
   * 8) 계정 잠금 해제
   * ================================================== */

  public int unlock(Long memberId) {
    return memberRepository.unlockMember(memberId);
  }

  /* ==================================================
   * 9) 삭제
   * ================================================== */

  public int delete(long memberId) {
    int cnt = 0;
    try {
      memberRepository.deleteById(memberId);
      cnt = 1;
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    return cnt;
  }

  /* ==================================================
   * 10) 이름 또는 이메일 검색
   * ================================================== */

  public List<Member> search(String keyword) {
    return memberRepository.searchByNameOrEmail(keyword);
  }
}
