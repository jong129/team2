package dev.jpa.team2.board_ai;

import lombok.Builder;
import lombok.Getter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class CategoryPolicyReader {

    private final JdbcTemplate jdbcTemplate;

    public CategoryPolicyReader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Getter
    @Builder
    public static class CategoryAiPolicy {
        private Long categoryId;
        private String aiSummaryYn;
        private String aiSentimentYn;
    }

    public CategoryAiPolicy getPolicyOrThrow(Long categoryId) {
        try {
            String sql = """
                SELECT CATEGORY_ID, AI_SUMMARY_YN, AI_SENTIMENT_YN
                FROM BOARD_CATEGORY
                WHERE CATEGORY_ID = ?
            """;

            CategoryAiPolicy policy = jdbcTemplate.queryForObject(sql, (rs, rn) -> CategoryAiPolicy.builder()
                    .categoryId(rs.getLong("CATEGORY_ID"))
                    .aiSummaryYn(rs.getString("AI_SUMMARY_YN"))
                    .aiSentimentYn(rs.getString("AI_SENTIMENT_YN"))
                    .build(), categoryId);

            if (policy == null) throw new ResponseStatusException(NOT_FOUND, "category not found");
            return policy;

        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(NOT_FOUND, "category not found");
        }
    }
}

