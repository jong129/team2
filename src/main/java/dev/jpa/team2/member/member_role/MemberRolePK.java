package dev.jpa.team2.member.member_role;

import java.io.Serializable;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MemberRolePK implements Serializable {

  private Long memberId;
  private Long roleId;
}
