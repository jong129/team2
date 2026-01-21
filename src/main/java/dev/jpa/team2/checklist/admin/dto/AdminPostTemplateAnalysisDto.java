package dev.jpa.team2.checklist.admin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 관리자용 POST 체크리스트 템플릿 만족도 분석 DTO
 *
 * - 관리자 AI 패널에서 사용
 * - 특정 POST 템플릿(templateId)에 대한
 *   참여자 수, 평균 만족도, 최근 코멘트 요약 제공
 */
@Getter
@AllArgsConstructor
public class AdminPostTemplateAnalysisDto {

    /**
     * 분석 대상 템플릿 ID
     */
    private Long templateId;

    /**
     * 만족도 조사에 참여한 사용자 수
     * (COMPLETED 세션 기준)
     */
    private long participantCount;

    /**
     * 평균 만족도 점수 (1~5)
     * 소수점 1자리 반올림
     */
    private double avgScore;

    /**
     * 최근 만족도 코멘트 목록
     */
    private List<CommentDto> recentComments;

    /**
     * 만족도 코멘트 DTO (내부 클래스)
     */
    @Getter
    @AllArgsConstructor
    public static class CommentDto {

        /**
         * 만족도 점수
         */
        private Integer score;

        /**
         * 사용자 코멘트 내용
         */
        private String comment;
    }
}
