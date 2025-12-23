package dev.jpa.team2.chatbot;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "UPLOADED_FILE_SEQ")
    @SequenceGenerator(name = "UPLOADED_FILE_SEQ", sequenceName = "SEQ_UPLOADED_FILE_ID", allocationSize = 1)
    @Column(name = "FILE_ID")
    private Long fileId;

    @Column(name = "SESSION_ID")
    private Long sessionId;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "FILE_TYPE")
    private String fileType;

    @Column(name = "FILE_SIZE")
    private Long fileSize;

    @Column(name = "STORAGE_PATH")
    private String storagePath;

    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;
}
