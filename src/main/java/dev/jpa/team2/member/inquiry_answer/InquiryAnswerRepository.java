package dev.jpa.team2.member.inquiry_answer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryAnswerRepository
    extends JpaRepository<InquiryAnswer, Long> {

  Optional<InquiryAnswer> findByInquiryId(Long inquiryId);
}
