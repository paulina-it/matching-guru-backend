package uk.bovykina.matching_guru.dto.match;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchCreateDto {
    private Long programmeYearId;
    private Long mentorId;
    private Long menteeId;
    private double compatibilityScore;
}
