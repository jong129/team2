package dev.jpa.team2.member.member_role;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberRoleDTO {

  private Long memberId;
  private Long roleId;

  public MemberRole toEntity() {
    return new MemberRole(memberId, roleId);
  }
}
