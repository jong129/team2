package dev.jpa.team2.checklist.pre;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사전 체크리스트(PRE) 조회 API
 *
 * 프론트(React)에서 이 API만 호출해도
 * "현재 ACTIVE인 사전 체크리스트"를 화면에 뿌릴 수 있음.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/checklists/pre")
public class PreChecklistCont {

    private final PreChecklistService preChecklistService;

    /**
     * 현재 사용 중인(ACTIVE) 사전 체크리스트 조회
     * GET checklists/pre/active
     */
    @GetMapping("/active")
    public ResponseEntity<PreChecklistDTO.PreChecklistRes> getActivePreChecklist() {
        return ResponseEntity.ok(preChecklistService.getActivePreChecklist());
    }
}
