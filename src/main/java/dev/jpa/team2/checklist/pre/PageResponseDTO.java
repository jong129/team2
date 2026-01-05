package dev.jpa.team2.checklist.pre;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 페이징 응답 DTO
 * - Spring Data Page를 프론트에서 쓰기 좋은 형태로 변환
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {

    private List<T> content;

    private int number;          // 현재 페이지 (0-base)
    private int size;            // 페이지 사이즈
    private long totalElements;  // 전체 데이터 수
    private int totalPages;      // 전체 페이지 수

    private boolean first;
    private boolean last;

    public static <T> PageResponseDTO<T> of(org.springframework.data.domain.Page<T> page) {
        return PageResponseDTO.<T>builder()
            .content(page.getContent())
            .number(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
    }
}
