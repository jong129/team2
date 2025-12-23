package dev.jpa.team2.checklist.template;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checklists/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        System.out.println("-> ChecklistTemplateController Created");
        this.templateService = templateService;
    }

    @GetMapping
    public TemplateDTO getTemplate(@RequestParam("type") String type) {
        return templateService.getTemplateByType(type);
    }
}
