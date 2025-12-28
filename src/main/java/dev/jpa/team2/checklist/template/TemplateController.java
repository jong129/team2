package dev.jpa.team2.checklist.template;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checklist/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        System.out.println("-> ChecklistTemplateController Created");
        this.templateService = templateService;
    }

    @GetMapping("/active")
    public TemplateDTO getActiveTemplate(@RequestParam("type") String type) {
        return templateService.getTemplateByType(type);
    }
}
