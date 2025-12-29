package dev.jpa.team2.member.repassword;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetDTO {

    private String resetCode;
    private String newPassword;
    private String confirmPassword;
}
