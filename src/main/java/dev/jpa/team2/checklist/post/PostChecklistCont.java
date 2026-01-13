package dev.jpa.team2.checklist.post;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.checklist.model.ChecklistHistoryRowDTO;
import dev.jpa.team2.checklist.model.ChecklistSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/checklists/post")
public class PostChecklistCont {

  private final PostChecklistService postChecklistService;

  // ✅ POST 세션 시작 (프론트: axios.post("/checklists/post/session/start", null, {
  // params:{memberId} }))
  @PostMapping("/session/start")
  public PostChecklistDTO startPostSession(@RequestParam("memberId") Long memberId,
      @RequestParam(value = "preSessionId", required = false) Long preSessionId) {
    return postChecklistService.startPostSession(memberId, preSessionId);
  }

  // ✅ 템플릿/아이템 로드
  @GetMapping("/session/{sessionId}")
  public PostChecklistResponseDTO getPostChecklist(@PathVariable("sessionId") Long sessionId) {
    return postChecklistService.getPostChecklist(sessionId);
  }

  // ✅ statuses (프론트: GET /checklists/post/session/{id}/statuses)
  @GetMapping("/session/{sessionId}/statuses")
  public List<PostChecklistStatusDTO> getPostStatuses(@PathVariable("sessionId") Long sessionId) {
    return postChecklistService.getPostStatuses(sessionId);
  }

  // ✅ 라디오 저장 (프론트: PATCH /checklists/post/session/{sid}/items/{itemId})
  @PatchMapping("/session/{sessionId}/items/{itemId}")
  public void updateCheckStatus(@PathVariable("sessionId") Long sessionId, @PathVariable("itemId") Long itemId,
      @RequestBody UpdateCheckStatusRequest req) {
    postChecklistService.updateCheckStatus(sessionId, itemId, req.getCheckStatus());
  }

  @PatchMapping("/session/{sessionId}/complete")
  public void complete(@PathVariable("sessionId") Long sessionId) {
    System.out.println("[POST COMPLETE] sessionId=" + sessionId);
    postChecklistService.forceCompletePostSession(sessionId);
  }

  public static class UpdateCheckStatusRequest {
    private String checkStatus;

    public String getCheckStatus() {
      return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
      this.checkStatus = checkStatus;
    }
  }

  @GetMapping("/history")
  public Page<ChecklistHistoryRowDTO> postHistory(@RequestParam("memberId") Long memberId,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "5") int size) {
    return postChecklistService.getPostHistoryDto(memberId, page, size);
  }

  @DeleteMapping("/session/{sessionId}")
  public void deletePostSession(@PathVariable("sessionId") Long sessionId) {
    postChecklistService.deletePostSession(sessionId);
  }

  @GetMapping("/session/{sessionId}/summary")
  public PostChecklistSummaryDTO summary(@PathVariable("sessionId") Long sessionId) {
    return postChecklistService.getSummary(sessionId);
  }

  //✅ 만족도 저장
  @PostMapping("/session/{sessionId}/satisfaction")
  public void saveSatisfaction(@PathVariable("sessionId") Long sessionId, @RequestBody SaveSatisfactionRequest req) {
    postChecklistService.saveSatisfaction(sessionId, req.getRating(), req.getCommentText());
  }

  //✅ 만족도 조회(있으면 반환, 없으면 null)
  @GetMapping("/session/{sessionId}/satisfaction")
  public PostChecklistSatisfactionDTO getSatisfaction(@PathVariable("sessionId") Long sessionId) {
    return postChecklistService.getSatisfaction(sessionId);
  }

}
