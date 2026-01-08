package dev.jpa.team2.admin.password;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.admin.AdminPasswordChangeHistoryRowDto;
import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;

@Service
public class PasswordChangeHistoryService {

  private final PasswordChangeHistoryRepository passwordChangeHistoryRepository;
  private final MemberRepository memberRepository;

  public PasswordChangeHistoryService(PasswordChangeHistoryRepository passwordChangeHistoryRepository,
                                      MemberRepository memberRepository) {
    this.passwordChangeHistoryRepository = passwordChangeHistoryRepository;
    this.memberRepository = memberRepository;
  }

  // C
  @Transactional
  public void record(Long memberId,
                     Long changedById,
                     String changeType,
                     String oldPasswordToStore) {

    if (memberId == null || changedById == null) return;

    Member member = memberRepository.findByMemberId(memberId);
    if (member == null) return;

    Member changedBy = memberRepository.findByMemberId(changedById);
    if (changedBy == null) return;

    PasswordChangeHistory h = new PasswordChangeHistory();
    h.setMember(member);
    h.setChangedBy(changedBy);
    h.setChangeType(changeType != null && !changeType.isBlank() ? changeType : "USER_CHANGE");

    // 보안 권장: null 또는 "***"만
    h.setOldPassword((oldPasswordToStore == null || oldPasswordToStore.isBlank()) ? null : oldPasswordToStore);

    passwordChangeHistoryRepository.save(h);
  }

  @Transactional
  public void recordSelf(Long memberId) {
    record(memberId, memberId, "USER_CHANGE", null);
  }

  // R (검색 + 페이징)
  @Transactional(readOnly = true)
  public Page<AdminPasswordChangeHistoryRowDto> search(String keyword,
                                                      String changeType,
                                                      LocalDateTime fromAt,
                                                      LocalDateTime toExclusive,
                                                      Pageable pageable) {
    return passwordChangeHistoryRepository.search(keyword, changeType, fromAt, toExclusive, pageable);
  }

  // D (기간 purge)
  @Transactional
  public int purgeByPeriod(LocalDateTime fromAt, LocalDateTime toExclusive) {
    return passwordChangeHistoryRepository.purgeByPeriod(fromAt, toExclusive);
  }
}

