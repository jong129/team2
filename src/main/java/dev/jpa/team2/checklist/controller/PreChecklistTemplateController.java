package dev.jpa.team2.checklist.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import dev.jpa.team2.checklist.dto.PreChecklistTemplateDto;
import dev.jpa.team2.checklist.service.PreChecklistTemplateService;

@RestController
@RequestMapping("/checklists/pre")
@RequiredArgsConstructor
public class PreChecklistTemplateController {

    private final PreChecklistTemplateService preChecklistTemplateService;

    /**
     * ✅ ACTIVE PRE 체크리스트 템플릿 조회
     * GET /checklists/pre/active
     */
    @GetMapping("/active")
    public ResponseEntity<PreChecklistTemplateDto> getActivePreTemplate() {

        return ResponseEntity.ok(
            preChecklistTemplateService.getActivePreTemplate()
        );
    }
}
