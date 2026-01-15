package dev.jpa.team2.board_ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiGenerateRequest {
    // 기존 결과가 있어도 새로 생성할지
    private boolean force = false;

    // 본문이 너무 길면 잘라서 보낼지 (토큰/비용/지연 방지)
    private boolean truncate = true;
}
