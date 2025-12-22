package dev.jpa.team2.member.member_role;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MemberRoleRepository
    extends JpaRepository<MemberRole, MemberRolePK> {

  /* ==========================================
   * 1) 회원에게 권한 부여
   * save() 자동 지원
   * ========================================== */

  /* ==========================================
   * 2) 회원 권한 전체 조회
   * ========================================== */

  @Query(value = """
      SELECT r.ROLE_NAME
      FROM MEMBER_ROLE mr
      JOIN ROLE r ON mr.ROLE_ID = r.ROLE_ID
      WHERE mr.MEMBER_ID = :memberId
      """, nativeQuery = true)
  public List<String> findRoleNamesByMemberId(
      @Param("memberId") Long memberId);

  /* ==========================================
   * 3) 특정 권한 보유 회원 조회
   * ========================================== */

  @Query(value = """
      SELECT m.MEMBER_ID, m.LOGIN_ID, m.EMAIL, r.ROLE_NAME
      FROM MEMBER_ROLE mr
      JOIN MEMBER m ON mr.MEMBER_ID = m.MEMBER_ID
      JOIN ROLE r ON mr.ROLE_ID = r.ROLE_ID
      WHERE r.ROLE_NAME = :roleName
      """, nativeQuery = true)
  public List<Object[]> findMembersByRoleName(
      @Param("roleName") String roleName);

  /* ==========================================
   * 4) 관리자 여부 확인
   * ========================================== */

  @Query(value = """
      SELECT COUNT(*)
      FROM MEMBER_ROLE mr
      JOIN ROLE r ON mr.ROLE_ID = r.ROLE_ID
      WHERE mr.MEMBER_ID = :memberId
        AND r.ROLE_NAME = 'ADMIN'
      """, nativeQuery = true)
  public int isAdmin(@Param("memberId") Long memberId);

  /* ==========================================
   * 5) 회원 권한 전체 삭제
   * ========================================== */

  @Transactional
  @Modifying
  @Query(value = """
      DELETE FROM MEMBER_ROLE
      WHERE MEMBER_ID = :memberId
      """, nativeQuery = true)
  public int deleteByMemberId(@Param("memberId") Long memberId);

  /* ==========================================
   * 6) 특정 권한 삭제
   * ========================================== */

  @Transactional
  @Modifying
  @Query(value = """
      DELETE FROM MEMBER_ROLE
      WHERE MEMBER_ID = :memberId
        AND ROLE_ID = :roleId
      """, nativeQuery = true)
  public int deleteRole(
      @Param("memberId") Long memberId,
      @Param("roleId") Long roleId);
}
