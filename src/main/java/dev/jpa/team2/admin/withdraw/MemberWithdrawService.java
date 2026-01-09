package dev.jpa.team2.admin.withdraw;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.admin.AdminMemberWithdrawRowDto;
import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;

@Service
public class MemberWithdrawService {

  private final MemberWithdrawRepository memberWithdrawRepository;
  private final MemberRepository memberRepository;

  public MemberWithdrawService(MemberWithdrawRepository memberWithdrawRepository,
                               MemberRepository memberRepository) {
    this.memberWithdrawRepository = memberWithdrawRepository;
    this.memberRepository = memberRepository;
  }

  // C
  @Transactional
  public void record(Long memberId, String reasonCode, String reasonText) {
    if (memberId == null) return;

    Member member = memberRepository.findByMemberId(memberId);
    if (member == null) return;

    MemberWithdraw w = new MemberWithdraw();
    w.setMember(member);
    w.setReasonCode(reasonCode);
    w.setReasonText(reasonText);

    memberWithdrawRepository.save(w);
  }

  // R (검색 + 페이징)
  @Transactional(readOnly = true)
  public Page<AdminMemberWithdrawRowDto> search(String keyword,
                                               LocalDateTime fromAt,
                                               LocalDateTime toExclusive,
                                               Pageable pageable) {
    return memberWithdrawRepository.search(keyword, fromAt, toExclusive, pageable);
  }

  // D (기간 purge)
  @Transactional
  public int purgeByPeriod(LocalDateTime fromAt, LocalDateTime toExclusive) {
    return memberWithdrawRepository.purgeByPeriod(fromAt, toExclusive);
  }
}

