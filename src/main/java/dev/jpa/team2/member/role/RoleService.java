package dev.jpa.team2.member.role;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleService {

  @Autowired
  RoleRepository roleRepository;

  public RoleService() {
    System.out.println("-> RoleService created");
  }

  /** 권한 생성 */
  public Role save(RoleDTO roleDTO) {
    return roleRepository.save(roleDTO.toEntity());
  }

  /** 전체 목록 */
  public List<Role> findAll() {
    return roleRepository.findAllByOrderByRoleIdAsc();
  }

  /** PK 조회 */
  public Role findByRoleId(Long roleId) {
    return roleRepository.findByRoleId(roleId);
  }

  /** 권한명 조회 */
  public Role findByRoleName(String roleName) {
    return roleRepository.findByRoleName(roleName);
  }

  /** 권한 수정 */
  public int update(RoleDTO roleDTO) {
    return roleRepository.updateRoleName(
        roleDTO.getRoleId(),
        roleDTO.getRoleName()
    );
  }

  /** 삭제 */
  public int delete(Long roleId) {
    int cnt = 0;
    try {
      roleRepository.deleteById(roleId);
      cnt = 1;
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    return cnt;
  }

  /** 중복 검사 */
  public int checkRoleName(String roleName) {
    return roleRepository.countByRoleName(roleName);
  }
}
