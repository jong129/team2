package dev.jpa.team2.board;

import dev.jpa.team2.board.photo.BoardPhoto;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BoardPhotoDto {
  private Long photoId;
  private Long boardId;
  private String savedName;

  public static BoardPhotoDto from(BoardPhoto e) {
    return new BoardPhotoDto(e.getPhotoId(), e.getBoardId(), e.getSavedName());
  }
}
