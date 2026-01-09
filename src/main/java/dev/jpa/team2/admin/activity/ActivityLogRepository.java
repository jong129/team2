package dev.jpa.team2.admin.activity;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.admin.AdminActivityLogRowDto;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

  @Query("""
    select new dev.jpa.team2.admin.AdminActivityLogRowDto(
      a.logId,
      m.memberId,
      m.loginId,
      m.name,
      a.actionType,
      a.actionDetail,
      a.actionAt,
      a.actionIp
    )
    from ActivityLog a
      join a.member m
    where (:keyword is null or :keyword = '' or
           lower(m.loginId) like lower(concat('%', :keyword, '%')) or
           lower(m.name)    like lower(concat('%', :keyword, '%')) or
           lower(a.actionType) like lower(concat('%', :keyword, '%')) or
           lower(a.actionDetail) like lower(concat('%', :keyword, '%')) or
           lower(a.actionIp) like lower(concat('%', :keyword, '%'))
    )
    and (:actionType is null or :actionType = '' or a.actionType = :actionType)
    and (:fromAt is null or a.actionAt >= :fromAt)
    and (:toExclusive is null or a.actionAt < :toExclusive)
    """)
  Page<AdminActivityLogRowDto> search(
      @Param("keyword") String keyword,
      @Param("actionType") String actionType,
      @Param("fromAt") LocalDateTime fromAt,
      @Param("toExclusive") LocalDateTime toExclusive,
      Pageable pageable
  );

  @Modifying
  @Query("""
    delete from ActivityLog a
    where (:actionType is null or :actionType = '' or a.actionType = :actionType)
      and (:fromAt is null or a.actionAt >= :fromAt)
      and (:toExclusive is null or a.actionAt < :toExclusive)
  """)
  int purgeByPeriod(
      @Param("actionType") String actionType,
      @Param("fromAt") LocalDateTime fromAt,
      @Param("toExclusive") LocalDateTime toExclusive
  );
}
