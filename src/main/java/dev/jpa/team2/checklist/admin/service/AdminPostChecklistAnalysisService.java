package dev.jpa.team2.checklist.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.admin.dto.AdminPostTemplateAnalysisDto;
import dev.jpa.team2.checklist.model.ChecklistSatisfaction;
import dev.jpa.team2.checklist.repository.ChecklistSatisfactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자용 POST 체크리스트 템플릿 만족도 분석 Service
 *
 * - 특정 POST 템플릿(templateId)에 대한
 *   참여자 수, 평균 만족도, 최근 코멘트 요약 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPostChecklistAnalysisService {

    private final ChecklistSatisfactionRepository satisfactionRepository;

    /**
     * POST 템플릿 만족도 분석
     */
    public AdminPostTemplateAnalysisDto analyze(Long templateId) {

        // 1️⃣ 참여자 수 (COMPLETED 세션 기준)
        long participantCount =
            satisfactionRepository.countByTemplateId(templateId);

        // 2️⃣ 평균 만족도 (null 방어)
        Double avg =
            satisfactionRepository.findAvgRatingByTemplateId(templateId);

        double avgScore =
            avg == null ? 0.0 : Math.round(avg * 10) / 10.0;

        // 3️⃣ 최근 만족도 코멘트 3개
        List<ChecklistSatisfaction> recentList =
            satisfactionRepository.findRecentByTemplateId(
                templateId,
                PageRequest.of(0, 3)
            );

        List<AdminPostTemplateAnalysisDto.CommentDto> comments =
            recentList.stream()
                // 코멘트 없는 데이터는 제외
                .filter(s ->
                    s.getCommentText() != null &&
                    !s.getCommentText().isBlank()
                )
                .map(s ->
                    new AdminPostTemplateAnalysisDto.CommentDto(
                        s.getRating(),
                        s.getCommentText()
                    )
                )
                .collect(Collectors.toList());

        // 4️⃣ DTO 조립
        AdminPostTemplateAnalysisDto result =
            new AdminPostTemplateAnalysisDto(
                templateId,
                participantCount,
                avgScore,
                comments
            );

        // 🔍 로그 (운영 시 유용)
        log.info(
            "[ADMIN][POST][ANALYSIS] templateId={}, participants={}, avgScore={}",
            templateId,
            participantCount,
            avgScore
        );

        return result;
    }
}
