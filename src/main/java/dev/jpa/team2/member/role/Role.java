package dev.jpa.team2.member.role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ROLE")
@Getter
@Setter
@ToString
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
  @SequenceGenerator(
      name = "role_seq",
      sequenceName = "SEQ_ROLE_ID",
      allocationSize = 1
  )
  @Column(name = "ROLE_ID")
  private Long roleId;

  @Column(name = "ROLE_NAME", nullable = false, unique = true)
  private String roleName;

  protected Role() {}

  /** 권한 생성용 생성자 */
  public Role(String roleName) {
    this.roleName = roleName;
  }
}
