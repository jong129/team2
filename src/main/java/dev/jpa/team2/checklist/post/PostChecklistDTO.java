package dev.jpa.team2.checklist.post;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostChecklistDTO {

    /**
     * 생성된 POST 체크리스트 세션 ID
     */
    private Long sessionId;

    /**
     * 적용된 POST 템플릿 ID
     */
    private Long templateId;

    /**
     * POST 그룹 코드 (POST_A / POST_B / POST_C / POST_D)
     */
    private String postGroupCode;

    /**
     * 사전 체크 결과로 생성된 프로필 키 ID
     */
    private Long profileKeyId;
}
