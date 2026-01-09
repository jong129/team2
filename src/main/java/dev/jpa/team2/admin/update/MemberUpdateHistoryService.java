package dev.jpa.team2.admin.update;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.admin.AdminMemberUpdateHistoryRowDto;
import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;

@Service
public class MemberUpdateHistoryService {

  private final MemberUpdateHistoryRepository memberUpdateHistoryRepository;
  private final MemberRepository memberRepository;

  public MemberUpdateHistoryService(MemberUpdateHistoryRepository memberUpdateHistoryRepository,
                                    MemberRepository memberRepository) {
    this.memberUpdateHistoryRepository = memberUpdateHistoryRepository;
    this.memberRepository = memberRepository;
  }

  // C
  @Transactional
  public void recordFieldChange(Long memberId,
                                Long changedById,
                                String fieldName,
                                String oldValue,
                                String newValue,
                                String changeType) {

    if (memberId == null || changedById == null) return;
    if (fieldName == null || fieldName.isBlank()) return;

    Member member = memberRepository.findByMemberId(memberId);
    if (member == null) return;

    Member changedBy = memberRepository.findByMemberId(changedById);
    if (changedBy == null) return;

    MemberUpdateHistory h = new MemberUpdateHistory();
    h.setMember(member);
    h.setChangedBy(changedBy);
    h.setFieldName(fieldName);
    h.setOldValue(oldValue);
    h.setNewValue(newValue);
    h.setChangeType(changeType != null && !changeType.isBlank() ? changeType : "USER_CHANGE");

    memberUpdateHistoryRepository.save(h);
  }

  @Transactional
  public void recordNameChange(Long memberId,
                               Long changedById,
                               String oldName,
                               String newName,
                               String changeType) {
    recordFieldChange(memberId, changedById, "NAME", oldName, newName, changeType);
  }

  // R (검색 + 페이징)
  @Transactional(readOnly = true)
  public Page<AdminMemberUpdateHistoryRowDto> search(String keyword,
                                                    String fieldName,
                                                    String changeType,
                                                    LocalDateTime fromAt,
                                                    LocalDateTime toExclusive,
                                                    Pageable pageable) {
    return memberUpdateHistoryRepository.search(keyword, fieldName, changeType, fromAt, toExclusive, pageable);
  }

  // D (기간 purge)
  @Transactional
  public int purgeByPeriod(LocalDateTime fromAt, LocalDateTime toExclusive) {
    return memberUpdateHistoryRepository.purgeByPeriod(fromAt, toExclusive);
  }
}
