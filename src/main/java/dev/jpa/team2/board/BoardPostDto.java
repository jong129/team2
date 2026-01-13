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

  private String secretYn;       // Y/N
  private Long viewCnt;

  private String pinnedYn;       // Y/N
  private String deletedYn;      // Y/N

  private String loginId;        // 목록/검색용
  private String writerName;     // 목록/검색용

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // ✅ 카테고리 기능 플래그 (프론트 UI 숨김 판단용)
  private String commentYn;      // Y/N
  private String likeYn;         // Y/N
  private String reportYn;       // Y/N
  private String fileYn;         // Y/N (첨부 UI 숨길 때)
  private Long likeCnt;     // 공감 수
  private String likedYn;   // 내가 공감했는지(Y/N)
  
  public static BoardPostDto from(BoardPost p) {
    // category는 이미 p.getCategory()로 쓰고 있으니 거기서 같이 꺼내면 됨
    var c = p.getCategory();

    return BoardPostDto.builder()
        .boardId(p.getBoardId())
        .memberId(p.getMemberId())
        .categoryId(c.getCategoryId())
        .title(p.getTitle())
        .content(p.getContent())
        .secretYn(p.getSecretYn())
        .viewCnt(p.getViewCnt())
        .pinnedYn(p.getPinnedYn())
        .deletedYn(p.getDeletedYn())
        .loginId(p.getLoginId())
        .writerName(p.getWriterName())
        .createdAt(p.getCreatedAt())
        .updatedAt(p.getUpdatedAt())

        // ✅ 여기 4개가 핵심
        .commentYn(normalizeYn(c.getCommentYn(), "Y"))
        .likeYn(normalizeYn(c.getLikeYn(), "Y"))
        .reportYn(normalizeYn(c.getReportYn(), "Y"))
        .fileYn(normalizeYn(c.getFileYn(), "Y"))

        .build();
  }

  private static String normalizeYn(String v, String def) {
    if (v == null) return def;
    String t = v.trim().toUpperCase();
    if ("Y".equals(t) || "N".equals(t)) return t;
    return def;
  }
}

