package dev.jpa.team2.checklist.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.jpa.team2.checklist.model.PostDecisionLog;
import dev.jpa.team2.checklist.dto.PostDecisionResponse;
import dev.jpa.team2.checklist.repository.PostDecisionLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostDecisionQueryService {

    private final PostDecisionLogRepository postDecisionLogRepository;

    /**
     * PRE 세션 기준 POST 분기 사유 조회
     */
    public PostDecisionResponse getDecisionByPreSession(Long preSessionId) {

        PostDecisionLog log =
            postDecisionLogRepository.findByPreSessionId(preSessionId)
                .stream()
                .findFirst()
                .orElse(null);

        if (log == null) {
            return null;
        }

        PostDecisionResponse res = new PostDecisionResponse();
        res.setPostGroupCode(log.getResultCode());
        res.setRiskScoreSum(
            log.getRiskScoreSum() == null
                ? null
                : log.getRiskScoreSum().doubleValue()
        );

        if (log.getHighRiskItemIds() != null) {
            List<String> ids =
                Arrays.asList(log.getHighRiskItemIds().split(","));
            res.setHighRiskItemIds(ids);
        }

        // 사용자용 메시지 (1차 버전: rule 기반)
        if ("POST_B".equals(log.getResultCode())) {
          res.setMessage(
              "사전 체크리스트에서 일부 중요 확인 항목이 미이행되어, "
            + "추가 확인이 필요한 사후 체크리스트(위험 수준)로 제공됩니다."
          );
      } else {
          res.setMessage(
              "사전 체크리스트에서 주요 항목이 정상적으로 확인되어, "
            + "일반 사후 체크리스트를 제공합니다."
          );
      }

        return res;
    }
}
