package dev.jpa.team2.checklist.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.dto.PreChecklistHistoryRowDto;
import dev.jpa.team2.checklist.dto.PreChecklistItemDto;
import dev.jpa.team2.checklist.dto.PreItemStatusDto;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.SessionStatus;
import dev.jpa.team2.checklist.model.ChecklistItem;
import dev.jpa.team2.checklist.model.ChecklistResponse;
import dev.jpa.team2.checklist.repository.ItemRepository;
import dev.jpa.team2.checklist.repository.ResponseRepository;
import dev.jpa.team2.checklist.repository.SessionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreChecklistQueryService {

    private final ItemRepository itemRepository;
    private final ResponseRepository responseRepository;
    private final SessionRepository sessionRepository; // ✅ 추가

    /**
     * PRE 체크리스트 항목 조회
     */
    @Transactional(readOnly = true)
    public List<PreChecklistItemDto> getItemsWithStatus(Long sessionId) {

      List<ChecklistItem> items =
          itemRepository.findBySessionId(sessionId);

      Map<Long, ChecklistResponse> responseMap =
          responseRepository.findBySessionId(sessionId)
              .stream()
              .collect(Collectors.toMap(
                  ChecklistResponse::getItemId,
                  r -> r
              ));

      List<PreChecklistItemDto> result = new ArrayList<>();

      for (ChecklistItem item : items) {
          ChecklistResponse resp = responseMap.get(item.getItemId());

          result.add(
              new PreChecklistItemDto(
                  item.getItemId(),
                  item.getItemOrder(),
                  item.getTitle(),
                  item.getDescription(),
                  item.getRequiredYn(),
                  resp != null ? resp.getCheckStatus() : null
              )
          );
      }

      return result;
  }



    /**
     * PRE 체크리스트 기록 조회
     */
    public Page<PreChecklistHistoryRowDto> getPreHistory(
        Long memberId,
        SessionStatus status,
        Date from,
        Date to,
        int page,
        int size
    ) {
        Pageable pageable =
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sessionId"));

        return sessionRepository
            .searchPreHistory(
                memberId,
                ChecklistPhase.PRE,
                status,
                from,
                to,
                pageable
            )
            .map(s -> new PreChecklistHistoryRowDto(
                s.getSessionId(),
                s.getStatus(),
                s.getStartedAt(),
                s.getCompletedAt()
            ));
    }

    
    @Transactional(readOnly = true)
    public List<PreItemStatusDto> getPreStatuses(Long sessionId) {

        return responseRepository
            .findBySessionId(sessionId)
            .stream()
            .map(r -> new PreItemStatusDto(
                r.getItemId(),
                r.getCheckStatus()
            ))
            .toList();
    }

}
