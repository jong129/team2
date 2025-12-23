package dev.jpa.team2.checklist.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateItemRepository
        extends JpaRepository<TemplateItem, Long> {

    /**
     * 특정 템플릿에 속한 항목을
     * ITEM_ORDER 기준으로 정렬 조회
     */
    List<TemplateItem>
    findByTemplate_TemplateIdOrderByItemOrderAsc(Long templateId);
}
