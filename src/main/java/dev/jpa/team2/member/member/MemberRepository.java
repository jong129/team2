package dev.jpa.team2.member.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

// Long: 식별자(PK) 타입
public interface MemberRepository extends JpaRepository<Member, Long> {

  /* ==================================================
   * 1) 중복 검사
   * ================================================== */

  /** 로그인 ID 중복 검사 */
  public int countByLoginId(String loginId);

  /** 이메일 중복 검사 */
  public int countByEmail(String email);

  /* ==================================================
   * 2) 회원 가입
   * save() 자동 지원
   * ================================================== */

  /* ==================================================
   * 3) 전체 목록
   * ================================================== */

  /** 전체 회원 목록 (회원번호 오름차순) */
  public List<Member> findAllByOrderByMemberIdAsc();

  /* ==================================================
   * 4) 조회
   * ================================================== */

  /** PK 조회 */
  public Member findByMemberId(Long memberId);

  /** 로그인 ID 조회 */
  public Member findByLoginId(String loginId);

  /** 이메일 조회 */
  public Member findByEmail(String email);

  /** 이름 + 이메일로 회원 조회 (아이디 찾기용) */
  public java.util.Optional<Member> findByNameAndEmail(String name, String email);
  
  /** 로그인ID + 이메일로 회원 조회 (비밀번호 재설정용) */
  Optional<Member> findByLoginIdAndEmail(String loginId, String email);
  /* ==================================================
   * 5) 로그인 (ID 또는 EMAIL)
   * ================================================== */

  @Query(value = """
      SELECT *
      FROM MEMBER
      WHERE LOGIN_ID = :loginInput
         OR EMAIL = :loginInput
      """, nativeQuery = true)
  public Member findByLoginIdOrEmail(@Param("loginInput") String loginInput);

  /* ==================================================
   * 6) 로그인 성공 처리
   * ================================================== */

  @Transactional
  @Modifying
  @Query(value = """
      UPDATE MEMBER
      SET
          FAILED_LOGIN_COUNT = 0,
          STATUS = 'ACTIVE',
          LAST_LOGIN_AT = SYSDATE
      WHERE MEMBER_ID = :memberId
      """, nativeQuery = true)
  public int loginSuccess(@Param("memberId") Long memberId);

  /* ==================================================
   * 7) 로그인 실패 처리
   * ================================================== */

  /** 로그인 실패 횟수 증가 */
  @Transactional
  @Modifying
  @Query(value = """
      UPDATE MEMBER
      SET
          FAILED_LOGIN_COUNT = FAILED_LOGIN_COUNT + 1,
          LAST_FAILED_LOGIN_AT = SYSDATE
      WHERE MEMBER_ID = :memberId
      """, nativeQuery = true)
  public int increaseFailCount(@Param("memberId") Long memberId);

  /** 계정 잠금 처리 */
  @Transactional
  @Modifying
  @Query(value = """
      UPDATE MEMBER
      SET
          STATUS = 'LOCKED',
          LOCKED_AT = SYSDATE
      WHERE MEMBER_ID = :memberId
      """, nativeQuery = true)
  public int lockMember(@Param("memberId") Long memberId);

  /* ==================================================
   * 8) 계정 잠금 해제
   * ================================================== */

  @Transactional
  @Modifying
  @Query(value = """
      UPDATE MEMBER
      SET
          STATUS = 'ACTIVE',
          FAILED_LOGIN_COUNT = 0,
          LOCKED_AT = NULL
      WHERE MEMBER_ID = :memberId
      """, nativeQuery = true)
  public int unlockMember(@Param("memberId") Long memberId);

  /* ==================================================
   * 9) 회원 정보 수정
   * ================================================== */

  @Transactional
  @Modifying
  @Query(value = """
      UPDATE MEMBER
      SET
          NAME = :name,
          PHONE = :phone,
          UPDATED_AT = SYSDATE
      WHERE MEMBER_ID = :memberId
      """, nativeQuery = true)
  public int updateProfile(
      @Param("name") String name,
      @Param("phone") String phone,
      @Param("memberId") Long memberId
  );

  /* ==================================================
   * 10) 비밀번호 변경
   * ================================================== */

  @Transactional
  @Modifying
  @Query(value = """
      UPDATE MEMBER
      SET
          PASSWORD = :password,
          UPDATED_AT = SYSDATE
      WHERE MEMBER_ID = :memberId
      """, nativeQuery = true)
  public int updatePassword(
      @Param("memberId") Long memberId,
      @Param("password") String password
  );

  /* ==================================================
   * 11) 회원 삭제
   * ================================================== */

  /** PK 기준 삭제 (JpaRepository 기본 제공) */
  // deleteById(Long memberId);

  /* ==================================================
   * 12) 이름 또는 이메일 검색
   * ================================================== */

  @Query(value = """
      SELECT *
      FROM MEMBER
      WHERE NAME LIKE '%' || :keyword || '%'
         OR EMAIL LIKE '%' || :keyword || '%'
      ORDER BY MEMBER_ID DESC
      """, nativeQuery = true)
  public List<Member> searchByNameOrEmail(@Param("keyword") String keyword);
}
