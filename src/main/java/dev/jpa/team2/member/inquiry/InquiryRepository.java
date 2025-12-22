package dev.jpa.team2.member.inquiry;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

  List<Inquiry> findAllByOrderByInquiryIdDesc();

  List<Inquiry> findByMemberIdOrderByInquiryIdDesc(Long memberId);

  @Modifying
  @Query(value = """
      UPDATE INQUIRY
      SET STATUS = :status
      WHERE INQUIRY_ID = :inquiryId
  """, nativeQuery = true)
  int updateStatus(Long inquiryId, String status);
}
