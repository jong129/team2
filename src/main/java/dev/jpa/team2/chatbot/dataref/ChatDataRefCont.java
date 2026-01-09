package dev.jpa.team2.chatbot.dataref;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j   
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatDataRefCont {

    private final ChatDataRefService chatDataRefService;

    // POST /api/chat/sessions/latest/context
    @PostMapping("/sessions/latest/context")
    public ResponseEntity<ChatDataRefDto.Response> attachLatestContext(
        @RequestBody ChatDataRefDto.Request req,
        HttpSession httpSession
    ) {
        try {
            Long memberId = AuthSessionUtil.requireMemberId(httpSession);

            // 요청 로그
            log.info(
                "[ChatDataRef] attachLatestContext called | memberId={} | refType={} | title={}",
                memberId,
                req.getRefType(),
                req.getTitle()
            );

            ChatDataRefDto.Response res =
                chatDataRefService.attachToLatestSession(memberId, req);

            // 성공 로그
            log.info(
                "[ChatDataRef] attach success | sessionId={} | refId={}",
                res.getSessionId(),
                res.getRefId()
            );

            return ResponseEntity.ok(res);

        } catch (IllegalArgumentException e) {
            // 입력값 오류
            log.warn(
                "[ChatDataRef] invalid request | message={}",
                e.getMessage()
            );
            throw e; // GlobalExceptionHandler 있으면 거기로 위임

        } catch (Exception e) {
            // 서버 오류
            log.error(
                "[ChatDataRef] attach failed | request={} ",
                req,
                e
            );
            throw e;
        }
    }
}
