package dev.jpa.team2.chatbot;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class UploadedFileController {

    private final UploadedFileService service;

    /**
     * 파일 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadedFileResponseDto> upload(
            @RequestParam MultipartFile file,
            @RequestParam Long sessionId) {

        UploadedFileRequestDto dto = new UploadedFileRequestDto();
        dto.setSessionId(sessionId);

        return ResponseEntity.ok(
            service.upload(file, dto)
        );
    }

    /**
     * 세션별 파일 목록 조회
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<UploadedFileListDto>> list(
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(
            service.findBySession(sessionId)
        );
    }
}
