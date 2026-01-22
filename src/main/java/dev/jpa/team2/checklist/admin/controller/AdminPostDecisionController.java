package dev.jpa.team2.checklist.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.admin.dto.AdminPostDecisionDebugResponse;
import dev.jpa.team2.checklist.admin.service.AdminPostDecisionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/ai")
@RequiredArgsConstructor
public class AdminPostDecisionController {

    private final AdminPostDecisionService adminPostDecisionService;

    /**
     * PRE 세션 기반 POST 분기 AI 디버그
     */
    @GetMapping("/post-decision/{preSessionId}")
    public AdminPostDecisionDebugResponse debugPostDecision(
            @PathVariable("preSessionId") Long preSessionId
    ) {
        return adminPostDecisionService.debugPostDecision(preSessionId);
    }
}
