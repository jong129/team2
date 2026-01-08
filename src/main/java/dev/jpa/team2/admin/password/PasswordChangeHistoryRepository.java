package dev.jpa.team2.admin.password;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.admin.AdminPasswordChangeHistoryRowDto;

public interface PasswordChangeHistoryRepository extends JpaRepository<PasswordChangeHistory, Long> {

  @Query("""
    select new dev.jpa.team2.admin.AdminPasswordChangeHistoryRowDto(
      h.changeId,

      m.memberId,
      m.loginId,
      m.name,

      cb.memberId,
      cb.loginId,
      cb.name,

      h.changeType,
      h.changedAt
    )
    from PasswordChangeHistory h
      join h.member m
      join h.changedBy cb
    where (:keyword is null or :keyword = '' or
           lower(m.loginId) like lower(concat('%', :keyword, '%')) or
           lower(m.name)    like lower(concat('%', :keyword, '%')) or
           lower(cb.loginId) like lower(concat('%', :keyword, '%')) or
           lower(cb.name)    like lower(concat('%', :keyword, '%'))
    )
    and (:changeType is null or :changeType = '' or h.changeType = :changeType)
    and (:fromAt is null or h.changedAt >= :fromAt)
    and (:toExclusive is null or h.changedAt < :toExclusive)
    """)
  Page<AdminPasswordChangeHistoryRowDto> search(
      @Param("keyword") String keyword,
      @Param("changeType") String changeType,
      @Param("fromAt") LocalDateTime fromAt,
      @Param("toExclusive") LocalDateTime toExclusive,
      Pageable pageable
  );

  @Modifying
  @Query("""
    delete from PasswordChangeHistory h
    where (:fromAt is null or h.changedAt >= :fromAt)
      and (:toExclusive is null or h.changedAt < :toExclusive)
  """)
  int purgeByPeriod(
      @Param("fromAt") LocalDateTime fromAt,
      @Param("toExclusive") LocalDateTime toExclusive
  );
}
