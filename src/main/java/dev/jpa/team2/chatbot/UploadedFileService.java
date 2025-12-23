package dev.jpa.team2.chatbot;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UploadedFileService {

    private final UploadedFileRepository repository;

    /**
     * 파일 업로드
     */
    public UploadedFileResponseDto upload(
            MultipartFile file,
            UploadedFileRequestDto dto) {

        // 실제 프로젝트에서는 파일 저장 로직 필요
        String storagePath = "/files/" + file.getOriginalFilename();

        UploadedFile entity = new UploadedFile();
        entity.setSessionId(dto.getSessionId());
        entity.setFileName(file.getOriginalFilename());
        entity.setFileType(file.getContentType());
        entity.setFileSize(file.getSize());
        entity.setStoragePath(storagePath);
        entity.setUploadedAt(LocalDateTime.now());

        UploadedFile saved = repository.save(entity);

        return new UploadedFileResponseDto(
            saved.getFileId(),
            saved.getFileName(),
            saved.getFileType(),
            saved.getFileSize(),
            saved.getUploadedAt()
        );
    }

    /**
     * 세션별 파일 목록 조회
     */
    public List<UploadedFileListDto> findBySession(Long sessionId) {
        return repository.findBySessionId(sessionId)
                .stream()
                .map(f -> new UploadedFileListDto(
                        f.getFileId(),
                        f.getFileName(),
                        f.getFileType(),
                        f.getFileSize()
                ))
                .collect(Collectors.toList());
    }
}
