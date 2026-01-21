package dev.jpa.team2.checklist.controller;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.dto.PreChecklistHistoryPageDto;
import dev.jpa.team2.checklist.enums.SessionStatus;
import dev.jpa.team2.checklist.service.PreChecklistQueryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/checklists/pre/history")
@RequiredArgsConstructor
public class PreChecklistHistoryController {

    private final PreChecklistQueryService preChecklistQueryService;

    @GetMapping
    public ResponseEntity<PreChecklistHistoryPageDto> getPreHistory(
        @RequestParam("memberId") Long memberId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "5") int size,
        @RequestParam(name = "status", required = false) SessionStatus status,
        @RequestParam(name = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
        @RequestParam(name = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to
    ) {
        return ResponseEntity.ok(
            new PreChecklistHistoryPageDto(
                preChecklistQueryService.getPreHistory(
                    memberId, status, from, to, page, size
                )
            )
        );
    }
}
