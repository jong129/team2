package dev.jpa.team2.checklist.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.dto.PostChecklistDto;
import dev.jpa.team2.checklist.dto.PostChecklistSatisfactionDto;
import dev.jpa.team2.checklist.dto.PostItemStatusDto;
import dev.jpa.team2.checklist.dto.PostStartResponse;
import dev.jpa.team2.checklist.service.PostChecklistQueryService;
import dev.jpa.team2.checklist.service.PostChecklistSessionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/checklists/post")
@RequiredArgsConstructor
public class PostChecklistSessionController {

  private final PostChecklistSessionService postChecklistService;
  private final PostChecklistQueryService postChecklistQueryService;

  /**
   * ✅ POST 체크리스트 세션 시작
   * POST /checklists/post/session/start?memberId=&preSessionId=
   */
  @PostMapping("/session/start")
  public ResponseEntity<PostStartResponse> startPostSession(
      @RequestParam(name = "memberId") Long memberId,
      @RequestParam(name = "preSessionId", required = false) Long preSessionId
  ) {
      return ResponseEntity.ok(
          postChecklistService.startPostSession(memberId, preSessionId)
      );
  }


  /**
   * ✅ POST 체크리스트 세션 화면 진입용 조회 GET /checklists/post/session/{sessionId}
   */
  @GetMapping("/session/{sessionId}")
  public ResponseEntity<PostChecklistDto> getPostChecklistSession(@PathVariable("sessionId") Long sessionId) {
    return ResponseEntity.ok(postChecklistQueryService.getPostChecklist(sessionId));
  }

  /**
   * ✅ POST 체크리스트 항목 상태 조회 GET /checklists/post/session/{sessionId}/statuses
   */
  @GetMapping("/session/{sessionId}/statuses")
  public ResponseEntity<List<PostItemStatusDto>> getPostStatuses(@PathVariable("sessionId") Long sessionId) {
    return ResponseEntity.ok(postChecklistQueryService.getPostStatuses(sessionId));
  }

  /**
   * ✅ POST 체크리스트 항목 상태 변경 PATCH
   * /checklists/post/session/{sessionId}/items/{itemId}
   */
  @PatchMapping("/session/{sessionId}/items/{itemId}")
  public ResponseEntity<Void> updatePostItemStatus(@PathVariable("sessionId") Long sessionId,
      @PathVariable("itemId") Long itemId, @RequestBody PostItemStatusDto dto) {
    postChecklistService.updateItemStatus(sessionId, itemId, dto.getCheckStatus());
    return ResponseEntity.ok().build();
  }

  /**
   * ✅ POST 체크리스트 세션 완료 처리
   * PATCH /checklists/post/session/{sessionId}/complete
   */
  @PatchMapping("/session/{sessionId}/complete")
  public ResponseEntity<Void> completePostSession(
      @PathVariable("sessionId") Long sessionId
  ) {
    System.out.println("### COMPLETE SESSION HIT: " + sessionId);
      postChecklistService.completeSession(sessionId);
      return ResponseEntity.ok().build();
  }

  /**
   * ✅ POST 체크리스트 만족도 조회
   * GET /checklists/post/session/{sessionId}/satisfaction
   */
  @GetMapping("/session/{sessionId}/satisfaction")
  public ResponseEntity<PostChecklistSatisfactionDto> getSatisfaction(
      @PathVariable(name = "sessionId") Long sessionId
  ) {
      PostChecklistSatisfactionDto dto =
          postChecklistQueryService.getSatisfaction(sessionId);

      // 만족도 없으면 200 + null (프론트에서 정상 처리)
      return ResponseEntity.ok(dto);
  }
  
  /**
   * ✅ POST 체크리스트 세션 삭제
   */
  @DeleteMapping("/session/{sessionId}")
  public ResponseEntity<Void> deletePostSession(
          @PathVariable("sessionId") Long sessionId
  ) {
      postChecklistService.deleteSession(sessionId);
      return ResponseEntity.noContent().build();
  }

  /**
   * ✅ POST 체크리스트 만족도 저장
   * POST /checklists/post/session/{sessionId}/satisfaction
   */
  @PostMapping("/session/{sessionId}/satisfaction")
  public ResponseEntity<Void> saveSatisfaction(
      @PathVariable("sessionId") Long sessionId,
      @RequestBody PostChecklistSatisfactionDto dto
  ) {
      postChecklistService.saveSatisfaction(
          sessionId,
          dto.getRating(),
          dto.getCommentText()
      );
      return ResponseEntity.ok().build();
  }

  
}
