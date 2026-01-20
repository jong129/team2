package dev.jpa.team2.admin_inquiries_reply;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminInquiryReplyRepository extends JpaRepository<AdminInquiryReply, Long> {

  Optional<AdminInquiryReply> findByInquiryId(Long inquiryId);

  boolean existsByInquiryId(Long inquiryId);
  
  public interface AdminInquiryReplyReadRepository extends JpaRepository<AdminInquiryReply, Long> {
    Optional<AdminInquiryReply> findByInquiryId(Long inquiryId);
  }
}
