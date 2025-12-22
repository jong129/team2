package dev.jpa.team2.member.member_role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "MEMBER_ROLE")
@IdClass(MemberRolePK.class)
@Getter
@Setter
@ToString
public class MemberRole {

  @Id
  @Column(name = "MEMBER_ID")
  private Long memberId;

  @Id
  @Column(name = "ROLE_ID")
  private Long roleId;

  protected MemberRole() {}

  /** 권한 부여용 생성자 */
  public MemberRole(Long memberId, Long roleId) {
    this.memberId = memberId;
    this.roleId = roleId;
  }
}
