package dev.jpa.team2.member.repassword;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dev.jpa.team2.member.repassword.PasswordResetRequestDTO;
import dev.jpa.team2.member.repassword.PasswordResetDTO;

@RestController
@RequestMapping("/member/repassword")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> request(
            @RequestBody PasswordResetRequestDTO dto) {

        passwordResetService.requestPasswordReset(
            dto.getLoginId(),
            dto.getEmail()
        );

        return ResponseEntity.ok("인증 메일이 발송되었습니다.");
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(
            @RequestBody PasswordResetDTO dto) {

        passwordResetService.resetPassword(
            dto.getResetCode(),
            dto.getNewPassword(),
            dto.getConfirmPassword()
        );

        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }
}
