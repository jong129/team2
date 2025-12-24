package dev.jpa.team2.member.role;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RoleRepository extends JpaRepository<Role, Long> {

  /* ===============================
   * 중복 검사
   * =============================== */

  public int countByRoleName(String roleName);

  /* ===============================
   * 조회
   * =============================== */

  public Role findByRoleId(Long roleId);

  public Role findByRoleName(String roleName);

  public List<Role> findAllByOrderByRoleIdAsc();

  /* ===============================
   * 수정
   * =============================== */

  @Transactional
  @Modifying
  @Query(value = """
      UPDATE ROLE
      SET ROLE_NAME = :roleName
      WHERE ROLE_ID = :roleId
      """, nativeQuery = true)
  public int updateRoleName(
      @Param("roleId") Long roleId,
      @Param("roleName") String roleName
  );
}
