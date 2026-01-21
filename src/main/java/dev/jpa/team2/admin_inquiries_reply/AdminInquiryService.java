package dev.jpa.team2.admin_inquiries_reply;

import dev.jpa.team2.admin.AdminInquiryDetailDto;
import dev.jpa.team2.admin.AdminInquiryReplyRequest;
import dev.jpa.team2.admin.AdminInquiryRowDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminInquiryService {

  Page<AdminInquiryRowDto> list(String status, Pageable pageable);

  AdminInquiryDetailDto detailAndMarkInProgress(Long inquiryId);

  Long reply(Long adminMemberId, AdminInquiryReplyRequest req);
}
