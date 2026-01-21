package dev.jpa.team2.checklist.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 아이템 마스터 목록 조회용 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminChecklistItemMasterRowDto {

    private Long itemMasterId;

    private String phase;          // PRE / POST

    private String postGroupCode;  // POST_A / POST_B (POST일 때만)

    private String title;

    private String description;

    private String activeYn;       // Y / N
}
