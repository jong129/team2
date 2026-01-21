package dev.jpa.team2.checklist.dto;

import java.util.List;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PreChecklistHistoryPageDto {

    private final List<PreChecklistHistoryRowDto> content;
    private final int number;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public PreChecklistHistoryPageDto(Page<PreChecklistHistoryRowDto> page) {
        this.content = page.getContent();
        this.number = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }
}
