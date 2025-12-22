package dev.jpa.team2.member.inquiry;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inquiry")
public class InquiryCont {

  @Autowired
  InquiryService inquiryService;

  /** 문의 등록 */
  @PostMapping("/save")
  public ResponseEntity<Inquiry> save(@RequestBody InquiryDTO dto) {
    return ResponseEntity.ok(inquiryService.save(dto));
  }

  /** 전체 문의 (관리자) */
  @GetMapping("/find_all")
  public ResponseEntity<List<Inquiry>> findAll() {
    return ResponseEntity.ok(inquiryService.findAll());
  }

  /** 내 문의 */
  @GetMapping("/my/{memberId}")
  public ResponseEntity<List<Inquiry>> myInquiry(@PathVariable Long memberId) {
    return ResponseEntity.ok(inquiryService.findByMember(memberId));
  }
}
