package dev.jpa.team2.admin;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

  @Query("""
    select new dev.jpa.team2.admin.AdminLoginHistoryRowDto(
      h.historyId,
      m.memberId,
      m.loginId,
      m.name,
      h.loginAt,
      h.loginIp,
      h.successYn
    )
    from LoginHistory h
      join h.member m
    where (:keyword is null or :keyword = '' or
           lower(m.loginId) like lower(concat('%', :keyword, '%')) or
           lower(m.name)    like lower(concat('%', :keyword, '%')) or
           lower(h.loginIp) like lower(concat('%', :keyword, '%'))
    )
    and (:fromAt is null or h.loginAt >= :fromAt)
    and (:toExclusive is null or h.loginAt < :toExclusive)
    """)
  Page<AdminLoginHistoryRowDto> search(
      @Param("keyword") String keyword,
      @Param("fromAt") LocalDateTime fromAt,
      @Param("toExclusive") LocalDateTime toExclusive,
      Pageable pageable
  );

  // 기간 삭제용(아래 2)에서 사용)
  @Modifying
  @Query("""
    delete from LoginHistory h
    where (:fromAt is null or h.loginAt >= :fromAt)
      and (:toExclusive is null or h.loginAt < :toExclusive)
  """)
  int purgeByPeriod(
      @Param("fromAt") LocalDateTime fromAt,
      @Param("toExclusive") LocalDateTime toExclusive
  );
}
