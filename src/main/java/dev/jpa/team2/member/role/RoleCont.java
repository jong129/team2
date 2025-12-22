package dev.jpa.team2.member.role;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
public class RoleCont {

  @Autowired
  private RoleService roleService;

  public RoleCont() {
    System.out.println("-> RoleController created");
  }

  /**
   * 권한 등록
   * http://localhost:9100/role/save
   */
  @PostMapping("/save")
  public ResponseEntity<Role> save(@RequestBody RoleDTO roleDTO) {
    return ResponseEntity.ok(roleService.save(roleDTO));
  }

  /**
   * 중복 검사
   * http://localhost:9100/role/check_name?roleName=ADMIN
   */
  @GetMapping("/check_name")
  public ResponseEntity<Integer> checkName(
      @RequestParam String roleName) {

    return ResponseEntity.ok(roleService.checkRoleName(roleName));
  }

  /**
   * 전체 목록
   * http://localhost:9100/role/find_all
   */
  @GetMapping("/find_all")
  public ResponseEntity<List<Role>> findAll() {
    return ResponseEntity.ok(roleService.findAll());
  }

  /**
   * 조회
   * http://localhost:9100/role/read/1
   */
  @GetMapping("/read/{roleId}")
  public ResponseEntity<Role> read(
      @PathVariable Long roleId) {

    return ResponseEntity.ok(roleService.findByRoleId(roleId));
  }

  /**
   * 수정
   * http://localhost:9100/role/update
   */
  @PutMapping("/update")
  public ResponseEntity<Integer> update(
      @RequestBody RoleDTO roleDTO) {

    return ResponseEntity.ok(roleService.update(roleDTO));
  }

  /**
   * 삭제
   * http://localhost:9100/role/delete/3
   */
  @DeleteMapping("/delete/{roleId}")
  public ResponseEntity<Integer> delete(
      @PathVariable Long roleId) {

    return ResponseEntity.ok(roleService.delete(roleId));
  }
}
