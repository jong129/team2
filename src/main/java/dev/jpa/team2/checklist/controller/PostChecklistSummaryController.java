package dev.jpa.team2.checklist.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.dto.PostChecklistSummaryDto;
import dev.jpa.team2.checklist.service.PostChecklistQueryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/checklists/post")
@RequiredArgsConstructor
public class PostChecklistSummaryController {

    private final PostChecklistQueryService postChecklistQueryService;

    /**
     * ✅ POST 체크리스트 요약
     * GET /checklists/post/session/{sessionId}/summary
     */
    @GetMapping("/session/{sessionId}/summary")
    public PostChecklistSummaryDto getPostSummary(
        @PathVariable("sessionId") Long sessionId
    ) {
        return postChecklistQueryService.getPostSummary(sessionId);
    }

}
