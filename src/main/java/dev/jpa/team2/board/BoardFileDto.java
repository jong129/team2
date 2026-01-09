package dev.jpa.team2.board;

import dev.jpa.team2.board.file.BoardFile;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardFileDto {
  private Long fileId;
  private Long boardId;
  private String originalName;
  private String savedName;
  private Long fileSize;
  private LocalDateTime createdAt;

  public static BoardFileDto from(BoardFile e) {
    BoardFileDto d = new BoardFileDto();
    d.setFileId(e.getFileId());
    d.setBoardId(e.getBoardId());
    d.setOriginalName(e.getOriginalName());
    d.setSavedName(e.getSavedName());
    d.setFileSize(e.getFileSize());
    d.setCreatedAt(e.getCreatedAt());
    return d;
  }
}
