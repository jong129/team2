package dev.jpa.team2.checklist.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.admin.dto.AdminChecklistItemMasterRowDto;
import dev.jpa.team2.checklist.admin.service.AdminChecklistItemMasterService;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 체크리스트 아이템 마스터 컨트롤러
 */
@RestController
@RequestMapping("/admin/checklists/item-masters")
@RequiredArgsConstructor
public class AdminChecklistItemMasterController {

    private final AdminChecklistItemMasterService itemMasterService;

    /**
     * 🔹 템플릿 편집 화면용 아이템 풀 조회
     *
     * GET /admin/checklists/item-masters/pool
     */
    @GetMapping("/pool")
    public Page<AdminChecklistItemMasterRowDto> getItemMasterPool(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "phase", required = false) String phase,
            @RequestParam(name = "postGroupCode", required = false) String postGroupCode,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "activeYn", defaultValue = "Y") String activeYn
    ) {
        return itemMasterService.getItemMasters(
                page,
                size,
                phase,
                postGroupCode,
                keyword,
                activeYn
        );
    }
}
