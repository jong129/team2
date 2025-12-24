package dev.jpa.team2.member.email;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@CrossOrigin(origins = "*") // React 연동용 (필요 시 도메인 제한)
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(
            EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    /* ==================================================
       1) 인증번호 발송
       POST /email/send
       body: { "email": "user@gmail.com" }
    ================================================== */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> send(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");

        Map<String, Object> result = new HashMap<>();

        try {
            emailVerificationService.createOrUpdate(email);
            result.put("success", true);
            result.put("message", "인증번호가 이메일로 발송되었습니다.");
        } catch (IllegalStateException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /* ==================================================
       2) 인증번호 검증
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
