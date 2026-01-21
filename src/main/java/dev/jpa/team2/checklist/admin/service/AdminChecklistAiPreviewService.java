package dev.jpa.team2.checklist.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.jpa.team2.checklist.admin.dto.AiPreviewRequest;
import dev.jpa.team2.checklist.admin.dto.AiPreviewResponse;
import dev.jpa.team2.checklist.admin.repository.ChecklistTemplateItemRepository;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminChecklistAiPreviewService {

    private final RestTemplate restTemplate;
    private final ChecklistTemplateItemRepository templateItemRepository;

    @Value("${llm.base-url}")
    private String aiServerUrl;

    public AiPreviewResponse previewAiChecklist(
            Long templateId,
            ChecklistPhase phase
    ) {

        // 1️⃣ 기존 템플릿 항목 title 조회
        List<String> baseItems =
                templateItemRepository.findItemTitlesByTemplateId(templateId);

        System.out.println("===== AI PREVIEW CALL START =====");
        System.out.println("AI SERVER URL = " + aiServerUrl);
        System.out.println("templateId = " + templateId);
        System.out.println("phase = " + phase);
        System.out.println("baseItems = " + baseItems);

        // 2️⃣ FastAPI 요청 생성
        AiPreviewRequest req = new AiPreviewRequest();
        req.setBaseItems(baseItems);
        req.setPhase(phase.name());

        // 3️⃣ FastAPI 호출
        AiPreviewResponse response = restTemplate.postForObject(
                aiServerUrl + "/checklist/ai/preview",
                req,
                AiPreviewResponse.class
        );

        System.out.println("AI PREVIEW RESPONSE = " + response);
        System.out.println("===== AI PREVIEW CALL END =====");

        return response;
    }
}
