package dev.jpa.team2.board_ai;

import lombok.Data;

@Data
public class AiWriteDraftRequest {
    // write 화면에서 사용자가 대충 써 둔 입력
    private String title;
    private String content;

    // 캐시 무시(매번 새로 생성) 옵션
    private boolean force = false;
}
