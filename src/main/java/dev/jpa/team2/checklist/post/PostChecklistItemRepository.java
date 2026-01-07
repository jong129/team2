package dev.jpa.team2.checklist.post;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.checklist.model.ChecklistItem;

public interface PostChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

  // ✅ PRE 핵심 문항 조회용 (ITEM_ORDER IN)
  List<ChecklistItem> findByTemplate_TemplateIdAndItemOrderIn(Long templateId, List<Integer> itemOrders);

  // (기존) POST 전체 아이템 조회용: ITEM_ORDER만 정렬 (단계 섞임 가능)
  List<ChecklistItem> findByTemplate_TemplateIdOrderByItemOrderAsc(Long templateId);

  // ✅ (추가) POST 화면용: 단계(CHECK_AREA) 순서 + ITEM_ORDER 정렬
  @Query(value = """
      SELECT *
      FROM CHECKLIST_ITEM
      WHERE TEMPLATE_ID = :templateId
        AND ACTIVE_YN = 'Y'
      ORDER BY
        CASE CHECK_AREA
          WHEN '잔금 전' THEN 1
          WHEN '잔금 지급 시' THEN 2
          WHEN '입주 당일' THEN 3
          WHEN '입주 후' THEN 4
          ELSE 99
        END,
        ITEM_ORDER ASC
      """, nativeQuery = true)
  List<ChecklistItem> findPostItemsOrdered(@Param("templateId") Long templateId);
}
