package dev.jpa.team2.checklist.admin.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import dev.jpa.team2.checklist.admin.dto.AdminChecklistTemplateRowDto;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.TemplateStatus;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 체크리스트 템플릿 Custom Repository 구현체
 */
@RequiredArgsConstructor
public class ChecklistTemplateRepositoryImpl implements ChecklistTemplateRepositoryCustom {

  private final EntityManager em;

  @Override
  public Page<AdminChecklistTemplateRowDto> findAdminTemplateList(ChecklistPhase phase, TemplateStatus status,
      String keyword, Pageable pageable) {

    /*
     * =============================== 0. 정렬 컬럼 / 방향 결정 (화이트리스트)
     * ===============================
     */
    String orderColumn = "t.templateId"; // 기본값
    String orderDir = "desc";

    if (pageable.getSort().isSorted()) {
      Sort.Order order = pageable.getSort().iterator().next();

      switch (order.getProperty()) {
      case "versionNo":
        orderColumn = "t.versionNo";
        break;
      case "status":
        orderColumn = "t.status";
        break;
      case "updatedAt":
        orderColumn = "t.updatedAt";
        break;
      case "templateId":
      default:
        orderColumn = "t.templateId";
        break;
      }

      orderDir = order.isDescending() ? "desc" : "asc";
    }

    /*
     * =============================== 1. JPQL (템플릿 + 항목 수 집계)
     * ===============================
     */
    StringBuilder jpql = new StringBuilder();
    jpql.append("""
            select
                t,
                count(ti),
                sum(case when ti.activeYn = dev.jpa.team2.checklist.enums.Yn.Y then 1 else 0 end)
            from ChecklistTemplate t
            left join t.templateItems ti
            where 1 = 1
        """);

    if (phase != null) {
      jpql.append(" and t.phase = :phase");
    }

    if (status != null) {
      jpql.append(" and t.status = :status");
    }

    if (keyword != null && !keyword.isBlank()) {
      jpql.append(" and lower(t.templateName) like :keyword");
    }

    jpql.append(" group by t");
    jpql.append(" order by ").append(orderColumn).append(" ").append(orderDir);

    TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);

    if (phase != null) {
      query.setParameter("phase", phase);
    }

    if (status != null) {
      query.setParameter("status", status);
    }

    if (keyword != null && !keyword.isBlank()) {
      query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
    }

    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());

    List<Object[]> rows = query.getResultList();

    /*
     * =============================== 2. count 쿼리 (템플릿 기준)
     * ===============================
     */
    StringBuilder countJpql = new StringBuilder();
    countJpql.append("""
            select count(t)
            from ChecklistTemplate t
            where 1 = 1
        """);

    if (phase != null) {
      countJpql.append(" and t.phase = :phase");
    }

    if (status != null) {
      countJpql.append(" and t.status = :status");
    }

    if (keyword != null && !keyword.isBlank()) {
      countJpql.append(" and lower(t.templateName) like :keyword");
    }

    TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);

    if (phase != null) {
      countQuery.setParameter("phase", phase);
    }

    if (status != null) {
      countQuery.setParameter("status", status);
    }

    if (keyword != null && !keyword.isBlank()) {
      countQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
    }

    long total = countQuery.getSingleResult();

    /*
     * =============================== 3. Object[] → DTO 변환
     * ===============================
     */
    List<AdminChecklistTemplateRowDto> content = rows.stream().map(row -> {
      ChecklistTemplate t = (ChecklistTemplate) row[0];
      Long itemCnt = (Long) row[1];
      Long activeItemCnt = (Long) row[2];

      return new AdminChecklistTemplateRowDto(t.getTemplateId(), t.getPhase().name(), t.getTemplateName(),
          t.getVersionNo(), t.getStatus().name(), itemCnt != null ? itemCnt.intValue() : 0,
          activeItemCnt != null ? activeItemCnt.intValue() : 0,
          t.getUpdatedAt() != null
              ? t.getUpdatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
              : null);
    }).toList();

    /*
     * =============================== 4. Page 반환 ===============================
     */
    return new PageImpl<>(content, pageable, total);
  }
}
