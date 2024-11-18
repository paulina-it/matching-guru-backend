package uk.bovykina.matching_guru.dto.match;

import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import lombok.Data;

@Data
public class MatchUpdateDto {
    private MatchStatus status;
}
