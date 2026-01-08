package dev.jpa.team2.admin.activity;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.admin.AdminActivityLogRowDto;
import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class ActivityLogService {

  private final ActivityLogRepository activityLogRepository;
  private final MemberRepository memberRepository;

  public ActivityLogService(ActivityLogRepository activityLogRepository,
                            MemberRepository memberRepository) {
    this.activityLogRepository = activityLogRepository;
    this.memberRepository = memberRepository;
  }

  // C
  @Transactional
  public void record(Long memberId, String actionType, String actionDetail, HttpServletRequest request) {
    if (memberId == null) return;
    if (actionType == null || actionType.isBlank()) return;

    Member member = memberRepository.findByMemberId(memberId);
    if (member == null) return;

    ActivityLog a = new ActivityLog();
    a.setMember(member);
    a.setActionType(actionType);
    a.setActionDetail(actionDetail);
    a.setActionIp(extractClientIp(request));
    a.setUserAgent(request != null ? request.getHeader("User-Agent") : null);

    activityLogRepository.save(a);
  }

  // R (검색 + 페이징)
  @Transactional(readOnly = true)
  public Page<AdminActivityLogRowDto> search(String keyword,
                                            String actionType,
                                            LocalDateTime fromAt,
                                            LocalDateTime toExclusive,
                                            Pageable pageable) {
    return activityLogRepository.search(keyword, actionType, fromAt, toExclusive, pageable);
  }

  // D (기간 purge)
  @Transactional
  public int purgeByPeriod(String actionType, LocalDateTime fromAt, LocalDateTime toExclusive) {
    return activityLogRepository.purgeByPeriod(actionType, fromAt, toExclusive);
  }

  private String extractClientIp(HttpServletRequest request) {
    if (request == null) return null;
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
    return request.getRemoteAddr();
  }
}
