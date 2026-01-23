package dev.jpa.team2.admin_reportlog;

import dev.jpa.team2.admin.AdminReportBoardRowDto;
import dev.jpa.team2.board.report.BoardReport; // ✅ 추가
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AdminReportLogQueryRepository extends Repository<BoardReport, Long> { // ✅ Object → BoardReport

  @Query(
      value = """
        SELECT
          b.BOARD_ID AS boardId,
          b.TITLE AS title,
          b.MEMBER_ID AS writerId,
          m.NAME AS writerNickname,
          b.CATEGORY_ID AS categoryId,
          c.CATEGORY_NAME AS categoryName,
          COUNT(r.REPORT_ID) AS reportCount,
          CAST(MAX(r.CREATED_AT) AS TIMESTAMP) AS lastReportedAt
        FROM BOARD_REPORT r
        JOIN BOARD b ON b.BOARD_ID = r.BOARD_ID
        LEFT JOIN MEMBER m ON m.MEMBER_ID = b.MEMBER_ID
        LEFT JOIN BOARD_CATEGORY c ON c.CATEGORY_ID = b.CATEGORY_ID
        WHERE 1=1
          AND (:keyword IS NULL OR b.TITLE LIKE '%' || :keyword || '%')
          AND (:categoryId IS NULL OR b.CATEGORY_ID = :categoryId)
          AND (:fromAt IS NULL OR r.CREATED_AT >= :fromAt)
          AND (:toExclusive IS NULL OR r.CREATED_AT < :toExclusive)
        GROUP BY
          b.BOARD_ID, b.TITLE, b.MEMBER_ID, m.NAME, b.CATEGORY_ID, c.CATEGORY_NAME
        HAVING COUNT(r.REPORT_ID) >= NVL(:minCount, 1)
        ORDER BY reportCount DESC, lastReportedAt DESC
        """,
      countQuery = """
        SELECT COUNT(*)
        FROM (
          SELECT r.BOARD_ID
          FROM BOARD_REPORT r
          JOIN BOARD b ON b.BOARD_ID = r.BOARD_ID
          WHERE 1=1
            AND (:keyword IS NULL OR b.TITLE LIKE '%' || :keyword || '%')
            AND (:categoryId IS NULL OR b.CATEGORY_ID = :categoryId)
            AND (:fromAt IS NULL OR r.CREATED_AT >= :fromAt)
            AND (:toExclusive IS NULL OR r.CREATED_AT < :toExclusive)
          GROUP BY r.BOARD_ID
          HAVING COUNT(*) >= NVL(:minCount, 1)
        )
        """,
      nativeQuery = true
  )
  Page<AdminReportBoardRowDto> searchBoards(@Param("keyword") String keyword,
                                           @Param("categoryId") Long categoryId,
                                           @Param("minCount") Long minCount,
                                           @Param("fromAt") LocalDateTime fromAt,
                                           @Param("toExclusive") LocalDateTime toExclusive,
                                           Pageable pageable);

  @Query(
      value = """
        SELECT
          r.REPORT_ID AS reportId,
          r.MEMBER_ID AS reporterId,
          m.NAME AS reporterNickname,
          r.REASON_CODE AS reasonCode,
          r.REASON_TEXT AS reasonText,
          CAST(r.CREATED_AT AS TIMESTAMP) AS createdAt
        FROM BOARD_REPORT r
        LEFT JOIN MEMBER m ON m.MEMBER_ID = r.MEMBER_ID
        WHERE r.BOARD_ID = :boardId
        ORDER BY r.CREATED_AT DESC
        """,
      nativeQuery = true
  )
  List<AdminReportItemRow> findReportItems(@Param("boardId") Long boardId);

  interface AdminReportItemRow {
    Long getReportId();
    Long getReporterId();
    String getReporterNickname();
    String getReasonCode();
    String getReasonText();
    LocalDateTime getCreatedAt();
  }
}

