package dev.jpa.team2.chatbot;

import jakarta.servlet.http.HttpSession;

// 세션에 로그인 정보가 없으면 막고, 있으면 회원 ID를 반환하는 공통 인증 헬퍼
//  현재 프로젝트는 Spring Security를 CORS·CSRF 관리 용도로만 사용하고,
//  실제 로그인 인증은 HttpSession 기반으로 직접 처리하고 있어
//  컨트롤러/서비스 단에서 로그인 필수 여부를 강제하기 위해 AuthSessionUtil을 사용했다.

public class AuthSessionUtil {

    private AuthSessionUtil() {}  // 실수로 new AuthSessionUtil() 못하게 막는 용도
    
    // 현재 로그인한 사용자의  memberId를 가져온다. 로그인 안되어 있으면 즉시 예외 발생
    public static Long requireMemberId(HttpSession session) {
        Object v = session.getAttribute("LOGIN_MEMBER_ID"); // 세션에서 로그인 정보 조회
        if (v == null) throw new RuntimeException("로그인이 필요합니다."); // 로그인 여부 검증
        return (Long) v;  // 회원 ID 반환
    }
}
