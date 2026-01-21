package dev.jpa.team2.checklist.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.team2.checklist.dto.PostDecisionResponse;
import dev.jpa.team2.checklist.service.PostDecisionQueryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/checklists/post")
@RequiredArgsConstructor
public class PostDecisionController {

    private final PostDecisionQueryService postDecisionQueryService;

    /**
     * POST 분기 사유 조회
     * GET /checklists/post/decision?preSessionId=
     */
    @GetMapping("/decision")
    public ResponseEntity<PostDecisionResponse> getPostDecision(
        @RequestParam(name = "preSessionId") Long preSessionId
    ) {
        PostDecisionResponse res =
            postDecisionQueryService.getDecisionByPreSession(preSessionId);

        return ResponseEntity.ok(res);
    }
}
