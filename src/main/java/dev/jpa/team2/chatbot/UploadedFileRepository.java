package dev.jpa.team2.chatbot;

import dev.jpa.team2.chatbot.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedFileRepository
        extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findBySessionId(Long sessionId);
}
