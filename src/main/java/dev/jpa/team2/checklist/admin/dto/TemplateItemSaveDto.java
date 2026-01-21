package dev.jpa.team2.checklist.admin.dto;

import dev.jpa.team2.checklist.enums.Yn;
import lombok.Getter;
import lombok.Setter;

/**
 * 템플릿 구성 저장 DTO
 * - 전체 교체 방식
 */
@Getter
@Setter
public class TemplateItemSaveDto {

    private Long itemMasterId;

    private Integer itemOrder;

    private Yn requiredYn;

    private Yn activeYn;
}
