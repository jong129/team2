package dev.jpa.team2.checklist.template.service;

import dev.jpa.team2.checklist.template.entity.ChecklistTemplate;
import dev.jpa.team2.checklist.template.entity.TemplateItem;
import dev.jpa.team2.checklist.template.repository.ChecklistTemplateRepository;
import dev.jpa.team2.checklist.template.repository.TemplateItemRepository;
import dev.jpa.team2.checklist.template.dto.TemplateResponseDto;
import dev.jpa.team2.checklist.template.dto.TemplateItemDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ChecklistTemplateService {

    private final ChecklistTemplateRepository templateRepository;
    private final TemplateItemRepository templateItemRepository;

    public ChecklistTemplateService(ChecklistTemplateRepository templateRepository,
                                    TemplateItemRepository templateItemRepository) {
        this.templateRepository = templateRepository;
        this.templateItemRepository = templateItemRepository;
    }

    /**
     * PRE / POST 템플릿 조회
     */
    public TemplateResponseDto getTemplateByType(String templateType) {

        // 1️⃣ 활성 + 최신 버전 템플릿 조회
        ChecklistTemplate template = templateRepository
                .findTopByTemplateTypeAndIsActiveYnOrderByVersionNoDesc(templateType, "Y")
                .orElseThrow(() ->
                        new IllegalArgumentException("활성화된 템플릿이 존재하지 않습니다. type=" + templateType)
                );

        // 2️⃣ 템플릿에 포함된 항목 조회 (정렬)
        List<TemplateItem> templateItems =
                templateItemRepository.findByTemplate_TemplateIdOrderByItemOrderAsc(
                        template.getTemplateId()
                );

        // 3️⃣ Entity → DTO 변환
        List<TemplateItemDto> itemDtos = templateItems.stream()
                .map(TemplateItemDto::fromEntity)
                .collect(Collectors.toList());

        // 4️⃣ 최종 응답 DTO 생성
        return TemplateResponseDto.from(template, itemDtos);
    }
}
