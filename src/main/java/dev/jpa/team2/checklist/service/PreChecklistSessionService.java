package dev.jpa.team2.checklist.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import dev.jpa.team2.checklist.dto.PostStartResponse;
import dev.jpa.team2.checklist.dto.PreChecklistSessionDto;
import dev.jpa.team2.checklist.dto.PreChecklistSessionItemDto;
import dev.jpa.team2.checklist.enums.CheckStatus;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.SessionStatus;
import dev.jpa.team2.checklist.enums.TemplateStatus;
import dev.jpa.team2.checklist.enums.Yn;
import dev.jpa.team2.checklist.model.ChecklistItem;
import dev.jpa.team2.checklist.model.ChecklistResponse;
import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import dev.jpa.team2.checklist.repository.ItemRepository;
import dev.jpa.team2.checklist.repository.ResponseRepository;
import dev.jpa.team2.checklist.repository.SessionRepository;
import dev.jpa.team2.checklist.repository.TemplateItemRepository;
import dev.jpa.team2.checklist.repository.TemplateRepository;

@Service
@RequiredArgsConstructor
public class PreChecklistSessionService {

  private final SessionRepository sessionRepository;
  private final TemplateRepository templateRepository;
  private final TemplateItemRepository templateItemRepository;
  private final ItemRepository itemRepository;
  private final ResponseRepository responseRepository;

  private final PostChecklistSessionService postChecklistService;

  /**
   * PRE 체크리스트 세션 시작
   */
  @Transactional
  public ChecklistSession startPreSession(Long memberId) {
    return sessionRepository
        .findTopByMemberIdAndPhaseAndStatusOrderBySessionIdDesc(memberId, ChecklistPhase.PRE, SessionStatus.IN_PROGRESS)
        .orElseGet(() -> createNewPreSession(memberId));
  }

  /**
   * 신규 PRE 세션 생성
   */
  private ChecklistSession createNewPreSession(Long memberId) {

    // 1️⃣ PRE 템플릿은 상태와 무관하게 단 1개 조회
    ChecklistTemplate template = templateRepository
        .findFirstByPhaseOrderByVersionNoDesc(ChecklistPhase.PRE)
        .orElseThrow(() ->
            new IllegalStateException("PRE 템플릿이 존재하지 않습니다.")
        );

    // 2️⃣ ACTIVE 상태가 아니면 사용자 사용 불가
    if (template.getStatus() != TemplateStatus.ACTIVE) {
      throw new IllegalStateException(
          "사전 체크리스트가 현재 초안 상태입니다. 관리자에게 문의하세요."
      );
    }

    // 3️⃣ PRE 세션 생성
    ChecklistSession session = new ChecklistSession();
    session.setMemberId(memberId);
    session.setPhase(ChecklistPhase.PRE);
    session.setStatus(SessionStatus.IN_PROGRESS);
    session.setTemplateId(template.getTemplateId());
    session.setDeletedYn(Yn.N);

    ChecklistSession savedSession = sessionRepository.save(session);

    // 4️⃣ 템플릿 구성 항목 → 세션 항목으로 복사
    List<ChecklistTemplateItem> templateItems =
        templateItemRepository.findByTemplate_TemplateIdOrderByItemOrderAsc(
            template.getTemplateId()
        );

    for (ChecklistTemplateItem ti : templateItems) {
      ChecklistItem item = new ChecklistItem();
      item.setSessionId(savedSession.getSessionId());
      item.setItemOrder(ti.getItemOrder());
      item.setRequiredYn(ti.getRequiredYn());
      item.setActiveYn(ti.getActiveYn());
      item.setTitle(ti.getItemMaster().getTitle());
      item.setDescription(ti.getItemMaster().getDescription());
      itemRepository.save(item);
    }

    return savedSession;
  }


  /**
   * 체크리스트 항목 상태 저장
   */
  @Transactional
  public void saveItemStatus(Long sessionId, Long itemId, String checkStatus) {

    CheckStatus status = CheckStatus.valueOf(checkStatus);

    ChecklistResponse response = responseRepository.findBySessionIdAndItemId(sessionId, itemId).orElseGet(() -> {
      ChecklistResponse r = new ChecklistResponse();
      r.setSessionId(sessionId);
      r.setItemId(itemId);
      return r;
    });

    response.setCheckStatus(status);
    response.setUpdatedAt(new Date());

    responseRepository.save(response);
  }

  /**
   * PRE 체크리스트 완료 처리
   */
  @Transactional
  public PostStartResponse completePreSession(Long sessionId) {

    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalStateException("세션이 존재하지 않습니다."));

    if (session.getStatus() == SessionStatus.COMPLETED) {
      throw new IllegalStateException("이미 완료된 세션입니다.");
    }

    // 1️⃣ PRE 세션 완료 처리
    session.setStatus(SessionStatus.COMPLETED);
    session.setCompletedAt(new Date());

    // 2️⃣ POST 세션 시작 요청 (분기 판단은 POST 서비스 책임)
    return postChecklistService.startPostSession(
        session.getMemberId(),
        session.getSessionId()
    );
  }




  /**
   * PRE 세션 리셋
   */
  @Transactional
  public void resetPreSession(Long sessionId) {

    responseRepository.deleteBySessionId(sessionId);

    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalStateException("세션이 존재하지 않습니다."));

    session.setStatus(SessionStatus.IN_PROGRESS);
    session.setCompletedAt(null);

    sessionRepository.save(session);
  }

  /**
   * PRE 세션 논리 삭제
   */
  @Transactional
  public void deletePreSession(Long sessionId, Long memberId) {

    ChecklistSession session = sessionRepository.findBySessionIdAndMemberId(sessionId, memberId)
        .orElseThrow(() -> new IllegalStateException("세션이 존재하지 않거나 삭제 권한이 없습니다."));

    if (session.getDeletedYn() == Yn.Y) {
      throw new IllegalStateException("이미 삭제된 세션입니다.");
    }

    session.setDeletedYn(Yn.Y);
    session.setDeletedAt(new Date());

    sessionRepository.save(session);
  }

  @Transactional
  public ChecklistSession startNewPreSession(Long memberId) {
    return createNewPreSession(memberId);
  }
  
  @Transactional(readOnly = true)
  public PreChecklistSessionDto getPreSession(Long sessionId) {

      ChecklistSession session = sessionRepository.findById(sessionId)
          .orElseThrow(() -> new IllegalStateException("세션 없음"));

      List<ChecklistItem> items =
          itemRepository.findBySessionIdOrderByItemOrderAsc(sessionId);

      return new PreChecklistSessionDto(
          session.getSessionId(),
          session.getTemplateId(),
          items.stream()
              .map(it -> new PreChecklistSessionItemDto(
                  it.getItemId(),          // ⭐️ 핵심
                  it.getCheckArea(),
                  it.getTitle(),
                  it.getDescription(),
                  it.getRequiredYn()
              ))
              .toList()
      );
}


}
