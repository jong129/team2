package dev.jpa.team2.checklist.model;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {

  // 같은 그룹(phase + postGroupCode)에서 ACTIVE 1개 찾기
  Optional<ChecklistTemplate> findFirstByPhaseAndPostGroupCodeAndStatusOrderByVersionNoDesc(Phase phase,
      String postGroupCode, TemplateStatus status);

  // 같은 그룹에서 최신 버전 번호 찾기 (v2 만들 때 필요)
  Optional<ChecklistTemplate> findFirstByPhaseAndPostGroupCodeOrderByVersionNoDesc(Phase phase, String postGroupCode);

  // PRE처럼 postGroupCode가 null인 경우용
  Optional<ChecklistTemplate> findFirstByPhaseAndPostGroupCodeIsNullAndStatusOrderByVersionNoDesc(Phase phase,
      TemplateStatus status);

  Optional<ChecklistTemplate> findFirstByPhaseAndPostGroupCodeIsNullOrderByVersionNoDesc(Phase phase);

  @Query("""
        select max(t.versionNo)
        from ChecklistTemplate t
        where t.phase = :phase
          and (
            (:postGroupCode is null and t.postGroupCode is null)
            or (t.postGroupCode = :postGroupCode)
          )
      """)
  Integer findMaxVersionNo(@Param("phase") Phase phase, @Param("postGroupCode") String postGroupCode);

}
