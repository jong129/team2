package dev.jpa.team2.chatbot;

import java.util.List;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupedSearchResultsDto {
    private String date; // "YYYY-MM-DD"
    private List<SearchResultDto> results;
}
