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

import dev.jpa.team2.checklist.dto.PreChecklistSessionDto;
import dev.jpa.team2.checklist.dto.PreChecklistSyncRequest;
import dev.jpa.team2.checklist.dto.PreItemStatusDto;
import dev.jpa.team2.checklist.dto.PreSessionStartResponseDto;
import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.service.PreChecklistQueryService;
import dev.jpa.team2.checklist.service.PreChecklistSessionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/checklists/pre")
@RequiredArgsConstructor
public class PreChecklistSessionController {

  private final PreChecklistSessionService preChecklistService;
  private final PreChecklistQueryService preChecklistQueryService;

  /**
   * ✅ PRE 체크리스트 세션 시작 POST /checklists/pre/session/start?memberId=?
   */
  @PostMapping("/session/start")
  public ResponseEntity<PreSessionStartResponseDto> startPreSession(@RequestParam("memberId") Long memberId) {
    ChecklistSession session = preChecklistService.startPreSession(memberId);

    return ResponseEntity.ok(
        new PreSessionStartResponseDto(session.getSessionId(), session.getTemplateId(), session.getStatus(), false));
  }

  @PostMapping("/session/start-new")
  public ResponseEntity<PreSessionStartResponseDto> startNewPreSession(@RequestParam("memberId") Long memberId) {
    ChecklistSession session = preChecklistService.startNewPreSession(memberId);

    return ResponseEntity
        .ok(new PreSessionStartResponseDto(session.getSessionId(), session.getTemplateId(), session.getStatus(), true));
  }

  /**
   * ✅ PRE 체크리스트 세션 리셋 POST /checklists/pre/session/{sessionId}/reset
   */
  @PostMapping("/session/{sessionId}/reset")
  public ResponseEntity<Void> resetPreSession(@PathVariable("sessionId") Long sessionId) {
    preChecklistService.resetPreSession(sessionId);
    return ResponseEntity.ok().build();
  }

  /**
   * ✅ PRE 체크리스트 완료 처리 PATCH /checklists/pre/session/{sessionId}/complete
   */
  @PatchMapping("/session/{sessionId}/complete")
  public ResponseEntity<Void> completePreSession(@PathVariable("sessionId") Long sessionId) {
    preChecklistService.completePreSession(sessionId);
    return ResponseEntity.ok().build();
  }

  /**
   * ✅ PRE 체크리스트 세션 삭제 (논리 삭제) DELETE
   * /checklists/pre/session/{sessionId}?memberId=
   */
  @DeleteMapping("/session/{sessionId}")
  public ResponseEntity<Void> deletePreSession(@PathVariable("sessionId") Long sessionId,
      @RequestParam("memberId") Long memberId) {
    preChecklistService.deletePreSession(sessionId, memberId);
    return ResponseEntity.ok().build();
  }

  /**
   * ✅ PRE 세션 상세 조회 (체크 화면용) GET /checklists/pre/session/{sessionId}
   */
  @GetMapping("/session/{sessionId}")
  public ResponseEntity<PreChecklistSessionDto> getPreSession(@PathVariable("sessionId") Long sessionId) {
    return ResponseEntity.ok(preChecklistService.getPreSession(sessionId));
  }

  @GetMapping("/session/{sessionId}/statuses")
  public ResponseEntity<List<PreItemStatusDto>> getStatuses(@PathVariable("sessionId") Long sessionId) {
    return ResponseEntity.ok(preChecklistQueryService.getPreStatuses(sessionId));
  }

  /**
   * ✅ PRE 체크리스트 상태 일괄 저장 (SYNC) PATCH /checklists/pre/session/{sessionId}/sync
   */
  @PatchMapping("/session/{sessionId}/sync")
  public ResponseEntity<Void> syncPreSession(@PathVariable("sessionId") Long sessionId,
      @RequestBody PreChecklistSyncRequest request) {
    preChecklistService.syncSession(sessionId, request);
    return ResponseEntity.ok().build();
  }

}
