package dev.jpa.team2.checklist.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.jpa.team2.checklist.enums.Yn;
import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import dev.jpa.team2.checklist.model.ChecklistTemplateItemId;

/**
 * 체크리스트 템플릿 구성 항목 Repository (관리자)
 */
@Repository
public interface ChecklistTemplateItemRepository
        extends JpaRepository<ChecklistTemplateItem, ChecklistTemplateItemId> {

    /**
     * 템플릿 구성 항목 조회
     * - itemOrder 오름차순
     */
    List<ChecklistTemplateItem>
        findByTemplate_TemplateIdOrderByItemOrderAsc(Long templateId);

    /**
     * 템플릿 구성 항목 전체 삭제
     */
    void deleteByTemplate_TemplateId(Long templateId);
    
    @Query("""
        select im.title
        from ChecklistTemplateItem tti
        join tti.itemMaster im
        where tti.template.templateId = :templateId
        order by tti.itemOrder asc
    """)
    List<String> findItemTitlesByTemplateId(@Param("templateId") Long templateId);

}
