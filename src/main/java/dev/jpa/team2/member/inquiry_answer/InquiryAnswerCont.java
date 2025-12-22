package dev.jpa.team2.member.inquiry_answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/answer")
public class InquiryAnswerCont {

  @Autowired
  InquiryAnswerService answerService;

  /** 관리자 답변 */
  @PostMapping("/save")
  public ResponseEntity<?> save(@RequestBody InquiryAnswerDTO dto) {
    return ResponseEntity.ok(answerService.save(dto));
  }
}
