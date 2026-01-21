package dev.jpa.team2.checklist.admin.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import dev.jpa.team2.checklist.admin.dto.AdminChecklistItemMasterRowDto;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.model.ChecklistItemMaster;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChecklistItemMasterRepositoryImpl
        implements ChecklistItemMasterRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<AdminChecklistItemMasterRowDto> findAdminItemMasters(
            ChecklistPhase phase,
            String postGroupCode,
            String keyword,
            String activeYn,
            Pageable pageable
    ) {

        /* =========================
         * 1. JPQL
         * ========================= */
        StringBuilder jpql = new StringBuilder();
        jpql.append("""
            select new dev.jpa.team2.checklist.admin.dto.AdminChecklistItemMasterRowDto(
                i.itemMasterId,
                i.phase,
                i.postGroupCode,
                i.title,
                i.description,
                i.activeYn
            )
            from ChecklistItemMaster i
            where 1 = 1
        """);

        if (phase != null) {
            jpql.append(" and i.phase = :phase");
        }

        if (postGroupCode != null && !postGroupCode.isBlank()) {
            jpql.append(" and i.postGroupCode = :postGroupCode");
        }

        if (activeYn != null) {
            jpql.append(" and i.activeYn = :activeYn");
        }

        if (keyword != null && !keyword.isBlank()) {
            jpql.append(" and lower(i.title) like :keyword");
        }

        jpql.append(" order by i.itemMasterId desc");

        TypedQuery<AdminChecklistItemMasterRowDto> query =
                em.createQuery(jpql.toString(), AdminChecklistItemMasterRowDto.class);

        if (phase != null) {
            query.setParameter("phase", phase.name());
        }

        if (postGroupCode != null && !postGroupCode.isBlank()) {
            query.setParameter("postGroupCode", postGroupCode);
        }

        if (activeYn != null) {
            query.setParameter("activeYn", activeYn);
        }

        if (keyword != null && !keyword.isBlank()) {
            query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<AdminChecklistItemMasterRowDto> content =
                query.getResultList();

        /* =========================
         * 2. count 쿼리
         * ========================= */
        StringBuilder countJpql = new StringBuilder();
        countJpql.append("""
            select count(i)
            from ChecklistItemMaster i
            where 1 = 1
        """);

        if (phase != null) {
            countJpql.append(" and i.phase = :phase");
        }

        if (postGroupCode != null && !postGroupCode.isBlank()) {
            countJpql.append(" and i.postGroupCode = :postGroupCode");
        }

        if (activeYn != null) {
            countJpql.append(" and i.activeYn = :activeYn");
        }

        if (keyword != null && !keyword.isBlank()) {
            countJpql.append(" and lower(i.title) like :keyword");
        }

        TypedQuery<Long> countQuery =
                em.createQuery(countJpql.toString(), Long.class);

        if (phase != null) {
            countQuery.setParameter("phase", phase.name());
        }

        if (postGroupCode != null && !postGroupCode.isBlank()) {
            countQuery.setParameter("postGroupCode", postGroupCode);
        }

        if (activeYn != null) {
            countQuery.setParameter("activeYn", activeYn);
        }

        if (keyword != null && !keyword.isBlank()) {
            countQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }

        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
