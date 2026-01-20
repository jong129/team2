package dev.jpa.team2.admin_inquiries_reply;

import dev.jpa.team2.member.mypage.UserInquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AdminInquiryRepository extends JpaRepository<UserInquiry, Long> {

  Page<UserInquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);

  Page<UserInquiry> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

  @Modifying
  @Query("update UserInquiry u set u.status = :status where u.inquiryId = :inquiryId")
  int updateStatus(@Param("inquiryId") Long inquiryId, @Param("status") String status);
}
