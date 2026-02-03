package dev.jpa.team2.checklist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.model.PostChecklistSummary;

public interface PostChecklistSummaryRepository
        extends JpaRepository<PostChecklistSummary, Long> {

    /** 세션별 요약 존재 여부 */
    boolean existsBySessionId(Long sessionId);

    /** 세션별 요약 조회 */
    Optional<PostChecklistSummary> findBySessionId(Long sessionId);
}
