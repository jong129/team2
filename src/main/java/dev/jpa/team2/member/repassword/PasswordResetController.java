package dev.jpa.team2.member.repassword;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member/repassword")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * 비밀번호 변경 (resetCode 기반)
     * - 비번찾기(재설정) 경로이므로 RESET_CHANGE 로그가 남아야 함
     */
    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody PasswordResetDTO dto) {

        // ✅ 기존 resetPassword -> resetPasswordWithHistory 로 변경
        passwordResetService.resetPasswordWithHistory(
                dto.getResetCode(),
                dto.getNewPassword(),
                dto.getConfirmPassword()
        );

        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    @PostMapping("/token")
    public ResponseEntity<?> issueToken(@RequestBody PasswordResetRequestDTO dto) {
        String resetCode =
                passwordResetService.createResetToken(
                        dto.getLoginId(),
                        dto.getEmail()
                );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "resetCode", resetCode
                )
        );
    }
}
