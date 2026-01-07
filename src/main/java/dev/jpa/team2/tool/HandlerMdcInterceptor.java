package dev.jpa.team2.tool;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HandlerMdcInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {

        if (handler instanceof HandlerMethod hm) {
            String controller = hm.getBeanType().getSimpleName();
            String method = hm.getMethod().getName();

            MDC.put("method", controller + "." + method);
        }

        return true;
    }

    // ❗ MDC.clear() 절대 여기서 하지 마라
}
