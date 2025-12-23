package dev.jpa.team2.chatbot;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadedFileListDto {

    private Long fileId;
    private String fileName;
    private String fileType;
    private Long fileSize;
}
