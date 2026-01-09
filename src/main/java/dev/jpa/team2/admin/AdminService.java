package dev.jpa.team2.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;
import dev.jpa.team2.tool.PageResponse;

@Service
public class AdminService {

  private final MemberRepository memberRepository;
  private final LoginHistoryRepository loginHistoryRepository;

  public AdminService(MemberRepository memberRepository,
                      LoginHistoryRepository loginHistoryRepository) {
    this.memberRepository = memberRepository;
    this.loginHistoryRepository = loginHistoryRepository;
  }

  /* ===============================
     1) 회원조회: 검색 + 페이징
     =============================== */
  public PageResponse<AdminMemberListDto> getMembers(String keyword, Pageable pageable) {
    // ✅ MemberRepository에 선언한 메서드명과 정확히 맞춰야 함
    // Page<AdminMemberListDto> page = memberRepository.searchAdminMembers(keyword, pageable);
    // 또는 Page<Member>로 가져오고 map 하는 방식이면 그에 맞춰 수정

    Page<AdminMemberListDto> page = memberRepository.searchAdminMembers(keyword, pageable);
    return PageResponse.from(page);
  }

  /* ===============================
     2) 로그인 이력: 검색 + 페이징 + 기간(from/to)
     =============================== */
  public PageResponse<AdminLoginHistoryRowDto> getLoginHistories(
      String keyword,
      LocalDate from,
      LocalDate to,
      Pageable pageable
  ) {
    LocalDateTime fromAt = (from == null) ? null : from.atStartOfDay();
    // ✅ to는 "포함" 처리(해당 날짜 23:59:59까지 포함) => 다음날 00:00 미만으로 자름
    LocalDateTime toExclusive = (to == null) ? null : to.plusDays(1).atStartOfDay();

    return PageResponse.from(
        loginHistoryRepository.search(keyword, fromAt, toExclusive, pageable)
    );
  }

  /* ===============================
     3) 로그인 이력: 기간 삭제
     =============================== */
  @Transactional
  public int purgeLoginHistories(LocalDate from, LocalDate to) {
    LocalDateTime fromAt = (from == null) ? null : from.atStartOfDay();
    LocalDateTime toExclusive = (to == null) ? null : to.plusDays(1).atStartOfDay();
    return loginHistoryRepository.purgeByPeriod(fromAt, toExclusive);
  }

  /* ===============================
     4) 로그인 이력: 단일 삭제
     =============================== */
  @Transactional
  public void deleteLoginHistory(Long historyId) {
    loginHistoryRepository.deleteById(historyId);
  }
}


