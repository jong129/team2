package dev.jpa.team2.board_ai;

import lombok.Builder;
import lombok.Getter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class BoardDataReader {

    private final JdbcTemplate jdbcTemplate;

    public BoardDataReader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Getter
    @Builder
    public static class BoardRow {
        private Long boardId;
        private Long categoryId;
        private String title;
        private String content;
        private String deletedYn;
    }

    public BoardRow getBoardOrThrow(Long boardId) {
        try {
            String sql = """
                SELECT BOARD_ID, CATEGORY_ID, TITLE, CONTENT, DELETED_YN
                FROM BOARD
                WHERE BOARD_ID = ?
            """;

            BoardRow row = jdbcTemplate.queryForObject(sql, (rs, rn) -> BoardRow.builder()
                    .boardId(rs.getLong("BOARD_ID"))
                    .categoryId(rs.getLong("CATEGORY_ID"))
                    .title(rs.getString("TITLE"))
                    .content(rs.getString("CONTENT"))
                    .deletedYn(rs.getString("DELETED_YN"))
                    .build(), boardId);

            if (row == null) throw new ResponseStatusException(NOT_FOUND, "board not found");
            if ("Y".equalsIgnoreCase(row.deletedYn)) throw new ResponseStatusException(NOT_FOUND, "board deleted");
            return row;

        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(NOT_FOUND, "board not found");
        }
    }
}
