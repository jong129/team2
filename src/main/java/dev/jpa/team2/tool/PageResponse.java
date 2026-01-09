package dev.jpa.team2.tool;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PageResponse<T> {

  private List<T> content;

  private int page;          // 0-based
  private int size;
  private long totalElements;
  private int totalPages;

  private boolean first;
  private boolean last;
  private boolean hasNext;
  private boolean hasPrev;

  public static <T> PageResponse<T> from(Page<T> pageData) {
    PageResponse<T> res = new PageResponse<>();
    res.setContent(pageData.getContent());
    res.setPage(pageData.getNumber());
    res.setSize(pageData.getSize());
    res.setTotalElements(pageData.getTotalElements());
    res.setTotalPages(pageData.getTotalPages());
    res.setFirst(pageData.isFirst());
    res.setLast(pageData.isLast());
    res.setHasNext(pageData.hasNext());
    res.setHasPrev(pageData.hasPrevious());
    return res;
  }
}
