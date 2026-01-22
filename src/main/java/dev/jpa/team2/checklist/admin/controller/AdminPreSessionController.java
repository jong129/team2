package dev.jpa.team2.checklist.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.admin.dto.AdminPreSessionRowDto;
import dev.jpa.team2.checklist.admin.service.AdminPreSessionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPreSessionController {

    private final AdminPreSessionService adminPreSessionService;

    /**
     * 최근 완료된 PRE 세션 목록 조회
     */
    @GetMapping("/pre-sessions")
    public List<AdminPreSessionRowDto> getCompletedPreSessions() {
        return adminPreSessionService.getCompletedPreSessions();
    }

    /**
     * 관리자용 PRE 세션 삭제
     */
    @DeleteMapping("/pre-sessions/{sessionId}")
    public void deletePreSession(
            @PathVariable("sessionId") Long sessionId
    ) {
        adminPreSessionService.deletePreSession(sessionId);
    }
}
