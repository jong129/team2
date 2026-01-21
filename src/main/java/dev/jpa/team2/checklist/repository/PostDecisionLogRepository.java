package dev.jpa.team2.checklist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.model.PostDecisionLog;

/**
 * ============================================
 * POST 체크리스트 분기 판단 로그 Repository
 * ============================================
 */
public interface PostDecisionLogRepository
        extends JpaRepository<PostDecisionLog, Long> {

    /**
     * PRE 세션 기준 분기 로그 조회
     */
    List<PostDecisionLog> findByPreSessionId(Long preSessionId);

    /**
     * POST 세션 기준 분기 로그 조회
     */
    List<PostDecisionLog> findByPostSessionId(Long postSessionId);

    /**
     * 결과 코드 기준 조회 (POST_A / POST_B)
     */
    List<PostDecisionLog> findByResultCode(String resultCode);
}
