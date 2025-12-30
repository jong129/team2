package dev.jpa.team2.member.email;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "EMAIL_VERIFICATION")
public class EmailVerification {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "email_verification_seq"
    )
    @SequenceGenerator(
        name = "email_verification_seq",
        sequenceName = "SEQ_EMAIL_VERIFICATION_ID",
        allocationSize = 1
    )
    @Column(name = "VERIFICATION_ID")
    private Long verificationId;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "VERIFY_CODE", nullable = false)
    private String verifyCode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "EXPIRES_AT", nullable = false)
    private Date expiresAt;

    @Column(name = "VERIFIED_YN", nullable = false)
    private String verifiedYn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "VERIFIED_AT")
    private Date verifiedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;
    
    public void resetVerified() {
      this.verifiedYn = "N";
      this.verifiedAt = null;
  }
    
    protected EmailVerification() {}

    /* ===============================
       인증 요청 생성자
    =============================== */
    public EmailVerification(String email, String verifyCode, Date expiresAt) {
        this.email = email;
        this.verifyCode = verifyCode;
        this.expiresAt = expiresAt;
        this.verifiedYn = "N";
        this.createdAt = new Date();
    }

    /* ===============================
       인증 성공 처리
    =============================== */
    public void verify() {
        this.verifiedYn = "Y";
        this.verifiedAt = new Date();
    }
    
    public void updateCode(String verifyCode, Date expiresAt) {
      this.verifyCode = verifyCode;
      this.expiresAt = expiresAt;
      this.verifiedYn = "N";
      this.verifiedAt = null;
  }


    /* ===== getter ===== */

    public Long getVerificationId() {
        return verificationId;
    }

    public String getEmail() {
        return email;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public String getVerifiedYn() {
        return verifiedYn;
    }

    public Date getVerifiedAt() {
        return verifiedAt;
    }
}
