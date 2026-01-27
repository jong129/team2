package dev.jpa.team2.checklist.ai;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.jpa.team2.checklist.dto.PreRiskExplanationDto;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PreRiskExplanationAiService {

    /**
     * PRE 체크리스트 결과를
     * "3줄 이내 요약된 위험 설명"으로 변환
     *
     * ❗ 실패 시 예외 throw → 상위 Service에서 fallback
     */
    public PreRiskExplanationDto generateExplanation(
            Double riskScoreSum,
            List<String> aiReasons
    ) {

        if (riskScoreSum == null) {
            throw new IllegalStateException("riskScoreSum is null");
        }

        if (aiReasons == null || aiReasons.isEmpty()) {
            throw new IllegalStateException("aiReasons is empty");
        }

        // ⚠️ 지금은 LLM mock
        // 실제 LLM 호출 시 아래 문자열이 프롬프트 입력이 됨
        String summary = buildThreeLineSummary(riskScoreSum, aiReasons);

        PreRiskExplanationDto dto = new PreRiskExplanationDto();
        dto.setSummary(summary);

        // ✅ 상세 reason은 노출하지 않음 (UX 보호)
        // 필요 시 "자세히 보기"용으로 확장 가능
        dto.setReasons(List.of());

        dto.setActions(List.of(
            "AI 분석 결과를 참고하여 계약 전 주요 위험 요소를 우선적으로 확인하시기를 권장드립니다."
        ));

        return dto;
    }

    /**
     * 여러 AI reason을 종합해
     * 최대 3줄 이내 요약 생성 (mock)
     */
    private String buildThreeLineSummary(
            Double riskScoreSum,
            List<String> aiReasons
    ) {

        // 실제 LLM 프롬프트에서는:
        // - "최대 3줄"
        // - "항목 나열 금지"
        // - "공통 위험 요인 중심 요약"
        // 을 명시하게 됨

        String riskLevel =
            riskScoreSum >= 70
                ? "보증금 회수에 영향을 줄 수 있는"
                : "계약 전 확인이 필요한";

        return String.format(
            "AI 분석 결과, %s 위험 요소가 반복적으로 발견되었습니다.\n" +
            "주요 원인은 권리관계 및 계약 주체 확인과 관련된 사항으로 나타났습니다.\n" +
            "계약 체결 전 해당 부분을 중심으로 재확인을 권장드립니다.",
            riskLevel
        );
    }
}
