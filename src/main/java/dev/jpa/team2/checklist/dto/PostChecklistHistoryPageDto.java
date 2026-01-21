package dev.jpa.team2.checklist.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Getter;

@Getter
public class PostChecklistHistoryPageDto {

    private final List<PostChecklistHistoryRowDto> content;
    private final int number;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public PostChecklistHistoryPageDto(Page<PostChecklistHistoryRowDto> page) {
        this.content = page.getContent();
        this.number = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }
}
