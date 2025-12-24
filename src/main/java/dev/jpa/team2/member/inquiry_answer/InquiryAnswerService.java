package dev.jpa.team2.member.inquiry_answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.member.inquiry.InquiryRepository;

@Service
@Transactional
public class InquiryAnswerService {

  @Autowired
  InquiryAnswerRepository answerRepository;

  @Autowired
  InquiryRepository inquiryRepository;

  public InquiryAnswer save(InquiryAnswerDTO dto) {
    inquiryRepository.updateStatus(dto.getInquiryId(), "ANSWERED");
    return answerRepository.save(dto.toEntity());
  }
}
