package dev.jpa.team2.admin_reportlog;

import dev.jpa.team2.admin.AdminReportBoardDetailDto;
import dev.jpa.team2.admin.AdminReportBoardRowDto;
import dev.jpa.team2.admin.AdminReportItemDto;
import dev.jpa.team2.board.BoardPost;
import dev.jpa.team2.board.BoardPostRepository;
import dev.jpa.team2.board.report.BoardReportRepository;
import dev.jpa.team2.member.member.Member;
import dev.jpa.team2.member.member.MemberRepository;
import dev.jpa.team2.member.member_role.MemberRoleService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminReportLogService {

  private final AdminReportLogQueryRepository queryRepository;

  private final BoardPostRepository boardPostRepository;
  private final BoardReportRepository boardReportRepository;

  private final MemberRepository memberRepository;
  private final MemberRoleService memberRoleService;

  @Transactional(readOnly = true)
  public Page<AdminReportBoardRowDto> searchBoards(Long adminId,
                                                  String keyword,
                                                  Long categoryId,
                                                  Long minCount,
                                                  LocalDateTime fromAt,
                                                  LocalDateTime toExclusive,
                                                  Pageable pageable) {
    requireAdmin(adminId);
    return queryRepository.searchBoards(normalize(keyword), categoryId, minCount, fromAt, toExclusive, pageable);
  }

  @Transactional(readOnly = true)
  public AdminReportBoardDetailDto detail(Long adminId, Long boardId) {
    requireAdmin(adminId);

    BoardPost post = boardPostRepository.findByBoardIdAndDeletedYn(boardId, "N")
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));

    AdminReportBoardDetailDto dto = new AdminReportBoardDetailDto();
    dto.setBoardId(post.getBoardId());
    dto.setTitle(post.getTitle());
    dto.setContent(post.getContent());

    dto.setWriterId(post.getMemberId());
    Member writer = memberRepository.findByMemberId(post.getMemberId());
    dto.setWriterNickname(writer != null ? writer.getName() : null);

    if (post.getCategory() != null) {
      dto.setCategoryId(post.getCategory().getCategoryId());
      dto.setCategoryName(post.getCategory().getCategoryName());
    }

    List<AdminReportLogQueryRepository.AdminReportItemRow> rows = queryRepository.findReportItems(boardId);

    dto.setReportCount((long) rows.size());
    dto.setLastReportedAt(rows.isEmpty() ? null : rows.get(0).getCreatedAt());

    dto.setReports(rows.stream().map(r -> {
      AdminReportItemDto it = new AdminReportItemDto();
      it.setReportId(r.getReportId());
      it.setReporterId(r.getReporterId());
      it.setReporterNickname(r.getReporterNickname());
      it.setReasonCode(r.getReasonCode());
      it.setReasonText(r.getReasonText());
      it.setCreatedAt(r.getCreatedAt());
      return it;
    }).collect(Collectors.toList()));

    return dto;
  }

  /**
   * 하드삭제
   * - 원칙: 너희가 이미 갖고 있는 "게시글 하드삭제 서비스"를 여기서 호출하는 게 최선
   * - 다만 지금은 시그니처를 못 받았으니, 안전하게 DB delete로 구현해 둠
   * - 기존 하드삭제가 있다면 아래 2줄을 "기존 delete 호출 1줄"로 교체
   */
  @Transactional
  public void deleteHard(Long adminId, Long boardId) {
    requireAdmin(adminId);

    // 존재 확인
    boardPostRepository.findByBoardIdAndDeletedYn(boardId, "N")
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));

    // 1) 신고 정리(선택) — FK CASCADE면 없어도 됨
    boardReportRepository.deleteByBoardId(boardId);

    // 2) 게시글 하드삭제
    //    ✅ 너희 기존 게시글 하드삭제 서비스가 있으면, 이 줄을 그 호출로 바꿔라.
    boardPostRepository.deleteById(boardId);
  }

  private void requireAdmin(Long memberId) {
    if (memberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
    if (!memberRoleService.isAdmin(memberId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin only");
  }

  private String normalize(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}
