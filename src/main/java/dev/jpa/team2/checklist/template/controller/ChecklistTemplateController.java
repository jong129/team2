package dev.jpa.team2.checklist.template.controller;

import dev.jpa.team2.checklist.template.dto.TemplateResponseDto;
import dev.jpa.team2.checklist.template.service.ChecklistTemplateService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checklists/templates")
public class ChecklistTemplateController {

    private final ChecklistTemplateService templateService;

    public ChecklistTemplateController(ChecklistTemplateService templateService) {
        System.out.println("-> ChecklistTemplateController Created");
        this.templateService = templateService;
    }

    @GetMapping
    public TemplateResponseDto getTemplate(@RequestParam("type") String type) {
        return templateService.getTemplateByType(type);
    }
}
