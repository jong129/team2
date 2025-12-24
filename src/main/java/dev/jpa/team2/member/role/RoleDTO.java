package dev.jpa.team2.member.role;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RoleDTO {

  private Long roleId;
  private String roleName;

  public Role toEntity() {
    return new Role(roleName);
  }
}
