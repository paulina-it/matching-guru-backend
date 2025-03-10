package uk.bovykina.matching_guru.dto.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;

@Data
@AllArgsConstructor
public class MatchCreateDto {
    private Long programmeYearId;
    private Long mentorId;
    private Long menteeId;
    private double compatibilityScore;
    private MatchStatus status;
}
