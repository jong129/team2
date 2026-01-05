package dev.jpa.team2.checklist.pre;

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

import lombok.RequiredArgsConstructor;


/**
 * * 사전 체크리스트(PRE) 조회 API * * 프론트(React)에서 이 API만 호출해도 "현재 ACTIVE인 사전 체크리스트"를
 * 화면에 뿌릴 수 있음.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/checklists/pre")
public class PreChecklistCont {
  private final PreChecklistService preChecklistService;

  /** * 현재 사용 중인(ACTIVE) 사전 체크리스트 조회 GET checklists/pre/active */
  @GetMapping("/active")
  public ResponseEntity<PreChecklistDTO.PreChecklistRes> getActivePreChecklist() {
    return ResponseEntity.ok(preChecklistService.getActivePreChecklist());
  }

  /**
   * (B-1) 사전 체크리스트 진행 세션 시작 - 진행중 세션이 있으면 그 세션을 반환 - 없으면 새로 생성해서 반환
   *
   * POST /checklists/pre/session/start?memberId=1
   */
  @PostMapping("/session/start")
  public ResponseEntity<PreChecklistDTO.SessionRes> startSession(
      @RequestParam("memberId") Long memberId
  ) {
      return ResponseEntity.ok(preChecklistService.createNewSession(memberId));
  }

  /**
   * (C) 체크리스트 항목 체크 상태 저장
   *
   * PATCH /checklists/pre/session/{sessionId}/items/{itemId}
   * body: { "checkStatus": "DONE" }
   */
  @PatchMapping("/session/{sessionId}/items/{itemId}")
  public ResponseEntity<Void> updateItemStatus(
      @PathVariable("sessionId") Long sessionId,
      @PathVariable("itemId") Long itemId,
      @RequestBody PreChecklistDTO.UpdateItemReq req
  ) {
    preChecklistService.updateItemStatus(sessionId, itemId, req);
    return ResponseEntity.ok().build();
  }
  
  /**
   * (D) 세션 요약/경고
   * GET /checklists/pre/session/{sessionId}/summary
   */
  @GetMapping("/session/{sessionId}/summary")
  public ResponseEntity<PreChecklistDTO.SummaryRes> getSummary(
      @PathVariable("sessionId") Long sessionId
  ) {
    return ResponseEntity.ok(preChecklistService.getSummary(sessionId));
  }
  
  /**
   * (E) 사전 체크리스트 세션 초기화
   *
   * POST /checklists/pre/session/{sessionId}/reset
   */
  @PostMapping("/session/{sessionId}/reset")
  public ResponseEntity<Void> resetSession(
      @PathVariable("sessionId") Long sessionId
  ) {
      preChecklistService.resetSession(sessionId);
      return ResponseEntity.noContent().build();
  }
  
  /**
   * (F) 이어하기용: 세션의 항목별 체크 상태 목록 조회
   * GET /checklists/pre/session/{sessionId}/statuses
   */
  @GetMapping("/session/{sessionId}/statuses")
  public ResponseEntity<java.util.List<PreChecklistDTO.ItemStatusRes>> getStatuses(
      @PathVariable("sessionId") Long sessionId
  ) {
      return ResponseEntity.ok(preChecklistService.getItemStatuses(sessionId));
  }

  /**
   * (H) 기록보기
   * GET /checklists/pre/history?memberId=1
   */
  @GetMapping("/history")
  public ResponseEntity<List<PreChecklistDTO.SessionHistoryItem>> getHistory(
      @RequestParam("memberId") Long memberId
  ) {
      return ResponseEntity.ok(preChecklistService.getPreHistory(memberId));
  }


  /**
   * (I) 세션 완료 처리
   * POST /checklists/pre/session/{sessionId}/complete?memberId=1
   */
  @PostMapping("/session/{sessionId}/complete")
  public ResponseEntity<Void> complete(
      @RequestParam("memberId") Long memberId,
      @PathVariable("sessionId") Long sessionId
  ) {
      preChecklistService.completeSession(memberId, sessionId);
      return ResponseEntity.ok().build();
  }

  /**
   * (J) 세션 삭제(Soft delete)
   * DELETE /checklists/pre/session/{sessionId}?memberId=1
   */
  @DeleteMapping("/session/{sessionId}")
  public ResponseEntity<Void> delete(
      @RequestParam("memberId") Long memberId,
      @PathVariable("sessionId") Long sessionId
  ) {
      preChecklistService.softDeleteSession(memberId, sessionId);
      return ResponseEntity.noContent().build();
  }




}