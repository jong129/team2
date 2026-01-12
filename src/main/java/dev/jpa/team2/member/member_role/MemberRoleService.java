package dev.jpa.team2.member.member_role;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberRoleService {

  @Autowired
  MemberRoleRepository memberRoleRepository;

  public MemberRoleService() {
    System.out.println("-> MemberRoleService created");
  }

  /** 권한 부여 */
  public MemberRole grantRole(MemberRoleDTO dto) {
    return memberRoleRepository.save(dto.toEntity());
  }

  /** 회원 권한 목록 */
  public List<String> findRoles(Long memberId) {
    return memberRoleRepository.findRoleNamesByMemberId(memberId);
  }

  /** 관리자 여부 */
  /** 관리자 여부 */
  public boolean isAdmin(Long memberId) {
    if (memberId == null)
      return false;

    Integer cnt = memberRoleRepository.isAdmin(memberId); // 여기 타입이 Long이면 Long으로
    return cnt != null && cnt > 0;
  }

  /** 권한 전체 삭제 */
  public int deleteAllRoles(Long memberId) {
    return memberRoleRepository.deleteByMemberId(memberId);
  }

  /** 특정 권한 삭제 */
  public int deleteRole(Long memberId, Long roleId) {
    return memberRoleRepository.deleteRole(memberId, roleId);
  }
}
