package dev.jpa.team2.chatbot;

import jakarta.servlet.http.HttpSession;

public class AuthSessionUtil {

    private AuthSessionUtil() {}

    public static Long requireMemberId(HttpSession session) {
        Object v = session.getAttribute("LOGIN_MEMBER_ID");
        if (v == null) throw new RuntimeException("로그인이 필요합니다.");
        return (Long) v;
    }
}
