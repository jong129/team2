package dev.jpa.team2.admin_inquiries_reply;

import dev.jpa.team2.admin.AdminInquiryDetailDto;
import dev.jpa.team2.admin.AdminInquiryReplyRequest;
import dev.jpa.team2.admin.AdminInquiryRowDto;
import dev.jpa.team2.member.mypage.UserInquiry;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminInquiryServiceImpl implements AdminInquiryService {

  private final AdminInquiryRepository adminInquiryRepository;
  private final AdminInquiryReplyRepository adminInquiryReplyRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<AdminInquiryRowDto> list(String status, Pageable pageable) {

    Page<UserInquiry> page;
    String s = (status == null) ? "" : status.trim();

    if (s.isEmpty() || "ALL".equalsIgnoreCase(s)) {
      page = adminInquiryRepository.findAllByOrderByCreatedAtDesc(pageable);
    } else {
      page = adminInquiryRepository.findByStatusOrderByCreatedAtDesc(s, pageable);
    }

    return page.map(x -> new AdminInquiryRowDto(
        x.getInquiryId(),
        x.getMemberId(),
        x.getTitle(),
        x.getCategory(),
        x.getStatus(),
        x.getCreatedAt()
    ));
  }

  @Override
  @Transactional
  public AdminInquiryDetailDto detailAndMarkInProgress(Long inquiryId) {
    UserInquiry inquiry = adminInquiryRepository.findById(inquiryId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "inquiry not found"));

    // RECEIVED -> IN_PROGRESS 자동 전환(관리자가 상세 열었을 때)
    if ("RECEIVED".equals(inquiry.getStatus())) {
      adminInquiryRepository.updateStatus(inquiryId, "IN_PROGRESS");
      inquiry.setStatus("IN_PROGRESS");
    }

    Optional<AdminInquiryReply> replyOpt = adminInquiryReplyRepository.findByInquiryId(inquiryId);

    if (replyOpt.isEmpty()) {
      return new AdminInquiryDetailDto(
          inquiry.getInquiryId(),
          inquiry.getMemberId(),
          inquiry.getTitle(),
          inquiry.getContent(),
          inquiry.getCategory(),
          inquiry.getStatus(),
          inquiry.getCreatedAt(),
          null,
          null,
          null,
          null
      );
    }

    AdminInquiryReply r = replyOpt.get();
    return new AdminInquiryDetailDto(
        inquiry.getInquiryId(),
        inquiry.getMemberId(),
        inquiry.getTitle(),
        inquiry.getContent(),
        inquiry.getCategory(),
        inquiry.getStatus(),
        inquiry.getCreatedAt(),
        r.getReplyId(),
        r.getMemberId(),
        r.getContent(),
        r.getAnsweredAt()
    );
  }

  @Override
  @Transactional
  public Long reply(Long adminMemberId, AdminInquiryReplyRequest req) {
    if (adminMemberId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
    if (req == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid body");

    Long inquiryId = req.getInquiryId();
    String content = (req.getContent() == null) ? "" : req.getContent().trim();

    if (inquiryId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inquiryId required");
    if (content.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content required");

    UserInquiry inquiry = adminInquiryRepository.findById(inquiryId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "inquiry not found"));

    // 1문의=1답변 정책(UK_AIR_INQUIRY) => 이미 답변 있으면 막기
    if (adminInquiryReplyRepository.existsByInquiryId(inquiryId)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "already answered");
    }

    AdminInquiryReply reply = new AdminInquiryReply();
    reply.setInquiryId(inquiryId);
    reply.setMemberId(adminMemberId);
    reply.setContent(req.getContent()); // 원문 유지

    AdminInquiryReply saved = adminInquiryReplyRepository.save(reply);

    // 답변 등록 성공 시 문의 상태는 CLOSED로 종료
    adminInquiryRepository.updateStatus(inquiryId, "CLOSED");
    inquiry.setStatus("CLOSED");

    return saved.getReplyId();
  }
}
