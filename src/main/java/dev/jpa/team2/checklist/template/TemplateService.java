package dev.jpa.team2.checklist.template;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateItemRepository templateItemRepository;

    public TemplateService(TemplateRepository templateRepository,
                                    TemplateItemRepository templateItemRepository) {
        this.templateRepository = templateRepository;
        this.templateItemRepository = templateItemRepository;
    }

    /**
     * PRE / POST 템플릿 조회
     */
    public TemplateDTO getTemplateByType(String templateType) {

        // 1️⃣ 활성 + 최신 버전 템플릿 조회
        Template template = templateRepository
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
        List<TemplateItemDTO> itemDtos = templateItems.stream()
                .map(TemplateItemDTO::fromEntity)
                .collect(Collectors.toList());

        // 4️⃣ 최종 응답 DTO 생성
        return TemplateDTO.from(template, itemDtos);
    }
}
