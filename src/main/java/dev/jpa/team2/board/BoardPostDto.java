package dev.jpa.team2.board;

import java.time.LocalDateTime;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPostDto {
  private Long boardId;
  private Long memberId;
  private Long categoryId;

  private String title;
  private String content;

  private String secretYn;       // Y/N ✅ 통일
  private Long viewCnt;

  private String pinnedYn;       // Y/N
  private String deletedYn;      // Y/N

  private String loginId;        // 목록/검색용
  private String writerName;     // 목록/검색용

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static BoardPostDto from(BoardPost p) {
    return BoardPostDto.builder()
        .boardId(p.getBoardId())
        .memberId(p.getMemberId())
        .categoryId(p.getCategory().getCategoryId())
        .title(p.getTitle())
        .content(p.getContent())
        .secretYn(p.getSecretYn())     // ✅ 변경
        .viewCnt(p.getViewCnt())
        .pinnedYn(p.getPinnedYn())
        .deletedYn(p.getDeletedYn())
        .loginId(p.getLoginId())
        .writerName(p.getWriterName())
        .createdAt(p.getCreatedAt())
        .updatedAt(p.getUpdatedAt())
        .build();
  }
}

