package dev.jpa.team2.tool;

import java.util.Map;

import org.jboss.logging.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
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

