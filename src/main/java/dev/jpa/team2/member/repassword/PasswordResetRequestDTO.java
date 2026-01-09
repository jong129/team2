package dev.jpa.team2.member.repassword;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequestDTO {

    private String loginId;
    private String email;
}
