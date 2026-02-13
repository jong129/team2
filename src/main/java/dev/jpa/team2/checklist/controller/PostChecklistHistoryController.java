package dev.jpa.team2.checklist.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.dto.PostChecklistHistoryPageDto;
import dev.jpa.team2.checklist.enums.SessionStatus;
import dev.jpa.team2.checklist.service.PostChecklistQueryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/checklists/post/history")
@RequiredArgsConstructor
public class PostChecklistHistoryController {

    private final PostChecklistQueryService postChecklistQueryService;

    @GetMapping
    public ResponseEntity<PostChecklistHistoryPageDto> getHistory(
        @RequestParam("memberId") Long memberId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "5") int size,
        @RequestParam(name = "status", required = false) SessionStatus status,
        @RequestParam(name = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(name = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(
            new PostChecklistHistoryPageDto(
                postChecklistQueryService.getPostHistory(
                    memberId, status, from, to, page, size
                )
            )
        );
    }
}
