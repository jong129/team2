package dev.jpa.team2.admin.update;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.admin.AdminMemberUpdateHistoryRowDto;

public interface MemberUpdateHistoryRepository extends JpaRepository<MemberUpdateHistory, Long> {

  @Query("""
    select new dev.jpa.team2.admin.AdminMemberUpdateHistoryRowDto(
      h.historyId,

      m.memberId,
      m.loginId,
      m.name,

      cb.memberId,
      cb.loginId,
      cb.name,

      h.fieldName,
      h.oldValue,
      h.newValue,
      h.changeType,
      h.changedAt
    )
    from MemberUpdateHistory h
      join h.member m
      join h.changedBy cb
    where (:keyword is null or :keyword = '' or
           lower(m.loginId) like lower(concat('%', :keyword, '%')) or
           lower(m.name)    like lower(concat('%', :keyword, '%')) or
           lower(cb.loginId) like lower(concat('%', :keyword, '%')) or
           lower(cb.name)    like lower(concat('%', :keyword, '%')) or
           lower(h.fieldName) like lower(concat('%', :keyword, '%')) or
           lower(h.oldValue)  like lower(concat('%', :keyword, '%')) or
           lower(h.newValue)  like lower(concat('%', :keyword, '%'))
    )
    and (:fieldName is null or :fieldName = '' or h.fieldName = :fieldName)
    and (:changeType is null or :changeType = '' or h.changeType = :changeType)
    and (:fromAt is null or h.changedAt >= :fromAt)
    and (:toExclusive is null or h.changedAt < :toExclusive)
    """)
  Page<AdminMemberUpdateHistoryRowDto> search(
      @Param("keyword") String keyword,
      @Param("fieldName") String fieldName,
      @Param("changeType") String changeType,
      @Param("fromAt") LocalDateTime fromAt,
      @Param("toExclusive") LocalDateTime toExclusive,
      Pageable pageable
  );

  @Modifying
  @Query("""
    delete from MemberUpdateHistory h
    where (:fromAt is null or h.changedAt >= :fromAt)
      and (:toExclusive is null or h.changedAt < :toExclusive)
  """)
  int purgeByPeriod(
      @Param("fromAt") LocalDateTime fromAt,
      @Param("toExclusive") LocalDateTime toExclusive
  );
}
