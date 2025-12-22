package dev.jpa.team2.member.member;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberDTO {

  private Long memberId;
  private String loginId;
  private String email;
  private String password;
  private String name;
  private String phone;
  private String status;
  private String emailVerified;
  private int failedLoginCount;
  private Date lastLoginAt;
  private Date lastFailedLoginAt;
  private Date lockedAt;
  private Date createdAt;
  private Date updatedAt;

  /**
   * DTO(Java) -> Entity(JPA)
   */
  public Member toEntity() {
    return new Member(
        this.loginId,
        this.email,
        this.password,
        this.name,
        this.phone
    );
}
}
