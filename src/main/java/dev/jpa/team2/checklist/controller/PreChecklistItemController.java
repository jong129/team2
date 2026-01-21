package dev.jpa.team2.checklist.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.dto.PreChecklistItemDto;
import dev.jpa.team2.checklist.service.PreChecklistQueryService;
import dev.jpa.team2.checklist.service.PreChecklistSessionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/checklists/pre/session/{sessionId}/items")
@RequiredArgsConstructor
public class PreChecklistItemController {

    private final PreChecklistQueryService preChecklistQueryService;
    private final PreChecklistSessionService preChecklistSessionService;

    /**
     * ✅ PRE 체크리스트 항목 조회
     */
    @GetMapping
    public ResponseEntity<List<PreChecklistItemDto>> getItems(
        @PathVariable("sessionId") Long sessionId
    ) {
        return ResponseEntity.ok(
            preChecklistQueryService.getItemsWithStatus(sessionId)
        );
    }

    /**
     * ✅ PRE 체크리스트 항목 상태 저장
     */
    @PatchMapping("/{itemId}")
    public ResponseEntity<Void> saveItemStatus(
        @PathVariable("sessionId") Long sessionId,
        @PathVariable("itemId") Long itemId,
        @RequestBody Map<String, String> body
    ) {
        preChecklistSessionService.saveItemStatus(
            sessionId,
            itemId,
            body.get("checkStatus")
        );
        return ResponseEntity.ok().build();
    }
}
