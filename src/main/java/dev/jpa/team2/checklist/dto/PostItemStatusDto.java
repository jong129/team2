package dev.jpa.team2.checklist.dto;

import dev.jpa.team2.checklist.enums.CheckStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * POST 체크리스트 항목 상태 변경 DTO
 *
 * JSON 예:
 * { "checkStatus": "DONE" }
 */
@Getter
@Setter
@NoArgsConstructor // 🔑 Jackson 역직렬화를 위한 기본 생성자
public class PostItemStatusDto {
    private Long itemId;
    private CheckStatus checkStatus;
}
