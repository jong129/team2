package dev.jpa.team2.admin.withdraw;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.admin.AdminMemberWithdrawRowDto;

public interface MemberWithdrawRepository extends JpaRepository<MemberWithdraw, Long> {

  @Query("""
    select new dev.jpa.team2.admin.AdminMemberWithdrawRowDto(
      w.withdrawId,
      m.memberId,
      m.loginId,
      m.name,
      w.reasonCode,
      w.reasonText,
      w.createdAt
    )
    from MemberWithdraw w
      join w.member m
    where (:keyword is null or :keyword = '' or
           lower(m.loginId) like lower(concat('%', :keyword, '%')) or
           lower(m.name)    like lower(concat('%', :keyword, '%')) or
           lower(w.reasonCode) like lower(concat('%', :keyword, '%')) or
           lower(w.reasonText) like lower(concat('%', :keyword, '%'))
    )
    and (:fromAt is null or w.createdAt >= :fromAt)
    and (:toExclusive is null or w.createdAt < :toExclusive)
    """)
  Page<AdminMemberWithdrawRowDto> search(
      @Param("keyword") String keyword,
      @Param("fromAt") LocalDateTime fromAt,
      @Param("toExclusive") LocalDateTime toExclusive,
      Pageable pageable
  );

  @Modifying
  @Query("""
    delete from MemberWithdraw w
    where (:fromAt is null or w.createdAt >= :fromAt)
      and (:toExclusive is null or w.createdAt < :toExclusive)
  """)
  int purgeByPeriod(
      @Param("fromAt") LocalDateTime fromAt,
      @Param("toExclusive") LocalDateTime toExclusive
  );
}
