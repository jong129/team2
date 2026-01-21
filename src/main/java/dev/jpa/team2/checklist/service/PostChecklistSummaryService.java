package dev.jpa.team2.checklist.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import dev.jpa.team2.checklist.dto.PostChecklistSummaryDto;

@Service
@RequiredArgsConstructor
public class PostChecklistSummaryService {

    /**
     * ✅ POST 체크리스트 요약 (임시 구현)
     * TODO: 실제 위험도/미완료 기반 로직으로 교체
     */
    public PostChecklistSummaryDto getSummary(Long sessionId) {

        // 임시 정책
        return new PostChecklistSummaryDto(
            "진행중",
            "아직 모든 항목이 완료되지 않았습니다."
        );
    }
}
