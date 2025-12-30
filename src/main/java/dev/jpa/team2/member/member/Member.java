package dev.jpa.team2.member.member;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Member {
  /**
   * 사원 번호, 식별자, sequence 자동 생성됨.
   * @Id: Primary Key
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
  @SequenceGenerator(name = "member_seq", sequenceName = "SEQ_MEMBER_ID", allocationSize = 1)
  @Column(name = "MEMBER_ID")
  private Long memberId;
  
  @Column(name = "LOGIN_ID", nullable = false, unique = true)
  private String loginId;

  @Column(name = "EMAIL", nullable = false, unique = true)
  private String email;

  @Column(name = "PASSWORD", nullable = false)
  private String password;

  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "PHONE")
  private String phone;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "EMAIL_VERIFIED")
  private String emailVerified;

  @Column(name = "FAILED_LOGIN_COUNT")
  private int failedLoginCount;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "LAST_LOGIN_AT")
  private Date lastLoginAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "LAST_FAILED_LOGIN_AT")
  private Date lastFailedLoginAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "LOCKED_AT")
  private Date lockedAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_AT")
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "UPDATED_AT")
  private Date updatedAt;
  
  protected Member() {
  
  }
  
  /* ===============================
  사용자 입력용 생성자
  =============================== */
  /**
   * 회원 가입 시 사용하는 생성자
   */
  public Member(String loginId, String email, String password, String name, String phone) {
    Date now = new Date();
    
    this.loginId = loginId;
    this.email = email;
    this.password = password;
    this.name = name;
    this.phone = phone;
    this.status = "ACTIVE";
    this.emailVerified = "N";
    this.failedLoginCount = 0;
    this.createdAt = now;   // 생성 시각
    this.updatedAt = now;   // 수정 시각
    
  }
  
  /* ===============================
  테스트 / 내부용 생성자
  =============================== */
  public Member(Long memberId, String loginId, String email, String password, String name) {
    
    this.loginId = loginId;
    this.email = email;
    this.password = password;
    this.name = name;
  }
}
