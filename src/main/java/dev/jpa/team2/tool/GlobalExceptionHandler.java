package dev.jpa.team2.tool;

import java.util.Map;

import org.jboss.logging.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusiness(BusinessException e) {

        ErrorCode ec = e.getErrorCode();

        MDC.put("error_code", String.valueOf(ec.getStatus()));

        log.warn("비즈니스 오류 발생: {}", e.getMessage());

        return ResponseEntity
                .status(ec.getStatus())
                .body(Map.of(
                        "error", ec.getMessage(),
                        "code", ec.getStatus()
                ));
    }

    // ✅ 추가: 401/403/404 같은 ResponseStatusException은 원래 상태코드로 그대로 내려보내기
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(ResponseStatusException e) {
        int code = e.getStatusCode().value();
        MDC.put("error_code", String.valueOf(code));

        // 4xx는 서버오류가 아니므로 error 로그 말고 warn 정도가 적절
        log.warn("요청 처리 오류: {} {}", code, e.getReason());

        return ResponseEntity
                .status(e.getStatusCode())
                .body(Map.of(
                        "error", e.getReason() != null ? e.getReason() : "error",
                        "code", code
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {

        MDC.put("error_code", "500");
        log.error("서버 오류", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "서버 오류",
                        "code", 500
                ));
    }
}

