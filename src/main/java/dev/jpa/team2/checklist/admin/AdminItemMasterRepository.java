package dev.jpa.team2.checklist.admin;

import dev.jpa.team2.checklist.model.ChecklistItemMaster;
import dev.jpa.team2.checklist.model.Phase;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AdminItemMasterRepository extends JpaRepository<ChecklistItemMaster, Long> {

  @Query("""
    select m
    from ChecklistItemMaster m
    where (:phase is null or m.phase = :phase)
      and (:postGroupCode is null or m.postGroupCode = :postGroupCode)
      and (:activeYn is null or m.activeYn = :activeYn)
      and (:keyword is null or lower(m.title) like lower(concat('%', :keyword, '%')))
  """)
  Page<ChecklistItemMaster> search(
      @Param("phase") Phase phase,
      @Param("postGroupCode") String postGroupCode,
      @Param("activeYn") String activeYn,
      @Param("keyword") String keyword,
      Pageable pageable
  );
}
