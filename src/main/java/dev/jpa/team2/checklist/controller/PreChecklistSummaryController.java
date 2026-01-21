package dev.jpa.team2.checklist.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import dev.jpa.team2.checklist.dto.PreChecklistSummaryDto;
import dev.jpa.team2.checklist.service.PreChecklistSummaryService;

@RestController
@RequestMapping("/checklists/pre")
@RequiredArgsConstructor
public class PreChecklistSummaryController {

    private final PreChecklistSummaryService summaryService;

    /**
     * ✅ PRE 체크리스트 요약
     * GET /checklists/pre/session/{sessionId}/summary
     */
    @GetMapping("/session/{sessionId}/summary")
    public ResponseEntity<PreChecklistSummaryDto> getSummary(
        @PathVariable("sessionId") Long sessionId
    ) {
        return ResponseEntity.ok(
            summaryService.getSummary(sessionId)
        );
    }

}
