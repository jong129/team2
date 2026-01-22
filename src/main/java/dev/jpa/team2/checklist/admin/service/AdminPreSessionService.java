package dev.jpa.team2.checklist.admin.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.admin.dto.AdminPreSessionRowDto;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.SessionStatus;
import dev.jpa.team2.checklist.enums.Yn;
import dev.jpa.team2.checklist.model.ChecklistSession;
import dev.jpa.team2.checklist.repository.ItemRepository;
import dev.jpa.team2.checklist.repository.ResponseRepository;
import dev.jpa.team2.checklist.repository.SessionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminPreSessionService {

  private final SessionRepository sessionRepository;
  private final ResponseRepository responseRepository;
  private final ItemRepository itemRepository;

  @Transactional(readOnly = true)
  public List<AdminPreSessionRowDto> getCompletedPreSessions() {

    return sessionRepository
        .findTop20ByPhaseAndStatusAndDeletedYnOrderByCompletedAtDesc(ChecklistPhase.PRE, SessionStatus.COMPLETED, Yn.N)

        .stream().map(s -> new AdminPreSessionRowDto(s.getSessionId(), s.getMemberId(), s.getCompletedAt())).toList();
  }

  @Transactional
  public void deletePreSession(Long sessionId) {

    ChecklistSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("세션이 존재하지 않습니다."));

    // ✅ PRE 세션만 삭제 허용
    if (session.getPhase() != ChecklistPhase.PRE) {
      throw new IllegalStateException("PRE 세션만 삭제할 수 있습니다.");
    }

    // ✅ 이미 삭제된 경우 방어
    if (session.getDeletedYn() == Yn.Y) {
      return;
    }

    // 1️⃣ 응답 삭제
    responseRepository.deleteBySessionId(sessionId);

    // 2️⃣ 세션 아이템 삭제
    itemRepository.deleteBySessionId(sessionId);

    // 3️⃣ 세션 소프트 삭제
    session.setDeletedYn(Yn.Y);
    session.setDeletedAt(new Date());

    sessionRepository.save(session);
  }

}
