package dev.jpa.team2.member.mypage;

import java.util.Date;

public class MyPageMeResDto {
  private Long memberId;
  private String loginId;
  private String email;
  private String name;
  private String phone;
  private String status;
  private Date createdAt;
  private Date updatedAt;

  public MyPageMeResDto(Long memberId, String loginId, String email, String name,
                        String phone, String status, Date createdAt, Date updatedAt) {
    this.memberId = memberId;
    this.loginId = loginId;
    this.email = email;
    this.name = name;
    this.phone = phone;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getMemberId() { return memberId; }
  public String getLoginId() { return loginId; }
  public String getEmail() { return email; }
  public String getName() { return name; }
  public String getPhone() { return phone; }
  public String getStatus() { return status; }
  public Date getCreatedAt() { return createdAt; }
  public Date getUpdatedAt() { return updatedAt; }
}
