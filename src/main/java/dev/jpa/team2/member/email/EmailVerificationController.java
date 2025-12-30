package dev.jpa.team2.member.email;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(
            EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    /* ==================================================
       1️⃣ 회원가입용 인증번호 발송
       POST /email/signup/send
       body: { "email": "user@gmail.com" }
    ================================================== */
    @PostMapping("/signup/send")
    public ResponseEntity<Map<String, Object>> sendForSignup(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");
        Map<String, Object> result = new HashMap<>();

        try {
            emailVerificationService.createForSignup(email);
            result.put("success", true);
            result.put("message", "회원가입 인증번호가 이메일로 발송되었습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /* ==================================================
       2️⃣ 비밀번호 재설정용 인증번호 발송
       POST /email/password/send
       body: { "email": "user@gmail.com" }
    ================================================== */
    @PostMapping("/password/send")
    public ResponseEntity<Map<String, Object>> sendForPasswordReset(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");
        Map<String, Object> result = new HashMap<>();

        try {
            emailVerificationService.createForPasswordReset(email);
            result.put("success", true);
            result.put("message", "비밀번호 재설정 인증번호가 이메일로 발송되었습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /* ==================================================
       3️⃣ 인증번호 검증 (공통)
       POST /email/verify
       body: { "email": "...", "code": "123456" }
    ================================================== */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");
        String code = body.get("code");

        Map<String, Object> result = new HashMap<>();

        try {
            emailVerificationService.verify(email, code);
            result.put("success", true);
            result.put("message", "이메일 인증이 완료되었습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}

