package dev.jpa.team2.member.inquiry;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InquiryService {

  @Autowired
  InquiryRepository inquiryRepository;

  public Inquiry save(InquiryDTO dto) {
    return inquiryRepository.save(dto.toEntity());
  }

  public List<Inquiry> findAll() {
    return inquiryRepository.findAllByOrderByInquiryIdDesc();
  }

  public List<Inquiry> findByMember(Long memberId) {
    return inquiryRepository.findByMemberIdOrderByInquiryIdDesc(memberId);
  }

  public Optional<Inquiry> findById(Long inquiryId) {
    return inquiryRepository.findById(inquiryId);
  }

  public int updateStatus(Long inquiryId, String status) {
    return inquiryRepository.updateStatus(inquiryId, status);
  }

  public int delete(Long inquiryId) {
    inquiryRepository.deleteById(inquiryId);
    return 1;
  }
}
