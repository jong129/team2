package dev.jpa.team2.checklist.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChecklistItemMasterRepository extends JpaRepository<ChecklistItemMaster, Long> {

  // (선택) 특정 phase/postGroupCode의 "활성 마스터"만 가져오고 싶을 때 유용
  @Query("""
    SELECT m
    FROM ChecklistItemMaster m
    WHERE m.activeYn = 'Y'
      AND m.phase = :phase
      AND (
        (:postGroupCode IS NULL AND m.postGroupCode IS NULL)
        OR (m.postGroupCode = :postGroupCode)
      )
    ORDER BY m.itemMasterId ASC
  """)
  List<ChecklistItemMaster> findActiveMasters(
      @Param("phase") Phase phase,
      @Param("postGroupCode") String postGroupCode
  );
}
