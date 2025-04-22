package uk.bovykina.matching_guru.dto.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;

@Data
@AllArgsConstructor
public class CoordinatorMatchDto {
    private Long id;

    private Long mentorId;
    private String mentorName;
    private String mentorEmail;

    private Long menteeId;
    private String menteeName;
    private String menteeEmail;

    private double compatibilityScore;
    private MatchStatus status;
}
