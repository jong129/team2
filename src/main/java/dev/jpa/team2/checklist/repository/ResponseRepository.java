package dev.jpa.team2.checklist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.checklist.enums.CheckStatus;
import dev.jpa.team2.checklist.model.ChecklistResponse;

public interface ResponseRepository extends JpaRepository<ChecklistResponse, Long> {

// 메인 페이지: 진행 여부 판단 (statuses)
  List<ChecklistResponse> findBySessionId(Long sessionId);

  Optional<ChecklistResponse> findBySessionIdAndItemId(Long sessionId, Long itemId);

// reset 시 응답 전체 삭제
  void deleteBySessionId(Long sessionId);

  /**
   * ❌ PRE 전용 (POST에서는 사용하지 않음)
   */
  @Query("""
      select count(r) > 0
      from ChecklistResponse r
      join ChecklistItem i on r.itemId = i.itemId
      where r.sessionId = :sessionId
        and i.requiredYn = 'Y'
        and r.checkStatus <> dev.jpa.team2.checklist.enums.CheckStatus.DONE
      """)
  boolean existsRequiredNotDone(@Param("sessionId") Long sessionId);

  /**
   * ✅ 특정 상태 존재 여부 (POST 완료 검증 핵심)
   */
  boolean existsBySessionIdAndCheckStatus(Long sessionId, CheckStatus checkStatus);

  List<ChecklistResponse> findBySessionIdAndCheckStatus(Long sessionId, CheckStatus checkStatus);

  @Modifying
  @Query("""
      update ChecklistResponse r
         set r.checkStatus = :status,
             r.updatedAt = CURRENT_TIMESTAMP
       where r.sessionId = :sessionId
      """)
  void updateStatusBySessionId(@Param("sessionId") Long sessionId, @Param("status") CheckStatus status);
}
