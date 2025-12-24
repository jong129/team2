package dev.jpa.team2.member.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSendService {

    private final JavaMailSender mailSender;

    public EmailSendService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /* ===============================
       인증 메일 발송
    =============================== */
    public void sendVerificationMail(String toEmail, String verifyCode) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[회원가입 이메일 인증]");
        message.setText(
            "회원가입을 위한 인증번호입니다.\n\n" +
            "인증번호: " + verifyCode + "\n\n" +
            "해당 인증번호는 5분간 유효합니다."
        );

        mailSender.send(message);
    }
}
