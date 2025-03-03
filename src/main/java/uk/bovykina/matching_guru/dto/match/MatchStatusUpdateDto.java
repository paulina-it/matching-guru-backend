package uk.bovykina.matching_guru.dto.match;

import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import lombok.Data;

import java.util.List;

@Data
public class MatchStatusUpdateDto {
    private List<Long> matchIds;
    private MatchStatus status;
}
