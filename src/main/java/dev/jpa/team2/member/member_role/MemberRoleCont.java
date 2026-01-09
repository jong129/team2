package dev.jpa.team2.member.member_role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member_role")
public class MemberRoleCont {

  @Autowired
  MemberRoleService memberRoleService;

  public MemberRoleCont() {
    System.out.println("-> MemberRoleController created");
  }

  /*
   * 권한 부여
   * http://localhost:9093/member_role/grant
   */
  @PostMapping("/grant")
  public ResponseEntity<MemberRole> grant(@RequestBody MemberRoleDTO dto) {
    return ResponseEntity.ok(memberRoleService.grantRole(dto));
  }

  /*
   * 회원 권한 조회
   * http://localhost:9093/member_role/roles/1
   */
  @GetMapping("/roles/{memberId}")
  public ResponseEntity<List<String>> roles(
      @PathVariable("memberId") Long memberId) {

    return ResponseEntity.ok(memberRoleService.findRoles(memberId));
  }

  /*
   * 관리자 여부 확인
   * http://localhost:9093/member_role/is_admin/1
   */
  @GetMapping("/is_admin/{memberId}")
  public ResponseEntity<Map<String, Object>> isAdmin(
      @PathVariable("memberId") Long memberId) {

    Map<String, Object> map = new HashMap<>();
    map.put("isAdmin", memberRoleService.isAdmin(memberId));
    return ResponseEntity.ok(map);
  }

  /*
   * 권한 전체 삭제
   * http://localhost:9093/member_role/delete_all/1
   */
  @DeleteMapping("/delete_all/{memberId}")
  public ResponseEntity<Integer> deleteAll(
      @PathVariable("memberId") Long memberId) {

    return ResponseEntity.ok(memberRoleService.deleteAllRoles(memberId));
  }

  /*
   * 특정 권한 삭제
   * http://localhost:9093/member_role/delete?memberId=1&roleId=2
   */
  @DeleteMapping("/delete")
  public ResponseEntity<Integer> deleteRole(
      @RequestParam("memberId") Long memberId,
      @RequestParam("roleId") Long roleId) {

    return ResponseEntity.ok(memberRoleService.deleteRole(memberId, roleId));
  }
}

