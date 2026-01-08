package dev.jpa.team2.checklist.post;

import dev.jpa.team2.checklist.model.ChecklistResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostChecklistResponseRepository
        extends JpaRepository<ChecklistResponse, Long> {

    // ✅ response.session.sessionId
    List<ChecklistResponse> findBySession_SessionId(Long sessionId);

    // (선택) 특정 항목 응답 찾을 때
    Optional<ChecklistResponse>
        findBySession_SessionIdAndItem_ItemId(Long sessionId, Long itemId);
}
