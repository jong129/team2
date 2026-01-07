package dev.jpa.team2.checklist.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.checklist.model.ChecklistTemplate;

public interface AdminChecklistTemplateListRepository extends JpaRepository<ChecklistTemplate, Long> {

  @Query(
      value = """
        SELECT
          TEMPLATE_ID     AS templateId,
          PHASE           AS phase,
          POST_GROUP_CODE AS postGroupCode,
          TEMPLATE_NAME   AS templateName,
          VERSION_NO      AS versionNo,
          STATUS          AS status,
          DESCRIPTION     AS description,
          CREATED_AT      AS createdAt,
          UPDATED_AT      AS updatedAt,
          ACTIVE_ITEM_CNT AS activeItemCnt,
          ITEM_CNT        AS itemCnt
        FROM VW_ADMIN_TEMPLATE_LIST
        WHERE (:phase IS NULL OR PHASE = :phase)
          AND (:status IS NULL OR STATUS = :status)
          AND (:keyword IS NULL OR LOWER(TEMPLATE_NAME) LIKE '%' || LOWER(:keyword) || '%')
        ORDER BY
          /* ✅ updatedAt 기준 */
          CASE WHEN :sortKey = 'updatedAt' AND :sortDir = 'asc'  THEN UPDATED_AT END ASC,
          CASE WHEN :sortKey = 'updatedAt' AND :sortDir = 'desc' THEN UPDATED_AT END DESC,

          /* ✅ templateId 기준 */
          CASE WHEN :sortKey = 'templateId' AND :sortDir = 'asc'  THEN TEMPLATE_ID END ASC,
          CASE WHEN :sortKey = 'templateId' AND :sortDir = 'desc' THEN TEMPLATE_ID END DESC,

          /* ✅ 안정적인 타이브레이커(항상 동일 결과) */
          UPDATED_AT DESC,
          TEMPLATE_ID DESC
      """,
      countQuery = """
        SELECT COUNT(*)
        FROM VW_ADMIN_TEMPLATE_LIST
        WHERE (:phase IS NULL OR PHASE = :phase)
          AND (:status IS NULL OR STATUS = :status)
          AND (:keyword IS NULL OR LOWER(TEMPLATE_NAME) LIKE '%' || LOWER(:keyword) || '%')
      """,
      nativeQuery = true
  )
  Page<AdminTemplateRow> findAdminTemplateRows(
      @Param("phase") String phase,
      @Param("status") String status,
      @Param("keyword") String keyword,
      @Param("sortKey") String sortKey,
      @Param("sortDir") String sortDir,
      Pageable pageable
  );
}

