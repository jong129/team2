package dev.jpa.team2.checklist.ai;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.model.ChecklistTemplate;

public interface AiPostChecklistStatRepository extends JpaRepository<ChecklistTemplate, Long> {

  // ✅ 활성 POST 템플릿 요약 목록
  @Query(value = """
    SELECT
      t.template_id         AS templateId,
      t.post_group_code     AS postGroupCode,
      t.version_no          AS versionNo,
      t.template_name       AS templateName,
      NVL(s.completed_cnt, 0) AS completedSessionCnt,
      s.avg_rating          AS avgRating
    FROM checklist_template t
    LEFT JOIN (
      SELECT
        cs.template_id AS template_id,
        COUNT(*)       AS completed_cnt,
        AVG(sat.rating) AS avg_rating
      FROM checklist_session cs
      LEFT JOIN checklist_satisfaction sat
        ON sat.session_id = cs.session_id
      WHERE cs.phase = 'POST'
        AND cs.status = 'COMPLETED'
        AND cs.deleted_yn = 'N'
      GROUP BY cs.template_id
    ) s
      ON s.template_id = t.template_id
    WHERE t.phase = 'POST'
      AND t.status = 'ACTIVE'
    ORDER BY t.post_group_code, t.version_no DESC
  """, nativeQuery = true)
  List<Object[]> listActivePostTemplateSummary();

  // ✅ 템플릿 1개의 아이템 통계
  @Query(value = """
      SELECT
        i.item_id      AS itemId,
        i.item_order   AS itemOrder,
        i.check_area   AS checkArea,
        i.title        AS title,
        i.required_yn  AS requiredYn,

        COUNT(DISTINCT cs.session_id) AS totalCnt,

        SUM(CASE WHEN NVL(r.check_status,'NOT_DONE') = 'DONE' THEN 1 ELSE 0 END) AS doneCnt,
        SUM(CASE WHEN NVL(r.check_status,'NOT_DONE') = 'NOT_DONE' THEN 1 ELSE 0 END) AS notDoneCnt,
        SUM(CASE WHEN NVL(r.check_status,'NOT_DONE') = 'NOT_REQUIRED' THEN 1 ELSE 0 END) AS notRequiredCnt,

        AVG(CASE WHEN NVL(r.check_status,'NOT_DONE') = 'DONE' THEN sat.rating END) AS avgRatingWhenDone,
        AVG(CASE WHEN NVL(r.check_status,'NOT_DONE') = 'NOT_DONE' THEN sat.rating END) AS avgRatingWhenNotDone

      FROM checklist_item i
      JOIN checklist_session cs
        ON cs.template_id = i.template_id
       AND cs.phase = 'POST'
       AND cs.status = 'COMPLETED'
       AND cs.deleted_yn = 'N'
      LEFT JOIN checklist_response r
        ON r.session_id = cs.session_id
       AND r.item_id = i.item_id
      LEFT JOIN checklist_satisfaction sat
        ON sat.session_id = cs.session_id
      WHERE i.template_id = :templateId
        AND i.active_yn = 'Y'
      GROUP BY i.item_id, i.item_order, i.check_area, i.title, i.required_yn
      ORDER BY i.item_order
    """, nativeQuery = true)
  List<Object[]> getPostItemStats(@Param("templateId") Long templateId);

}
