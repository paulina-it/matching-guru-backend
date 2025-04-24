package uk.bovykina.matching_guru.dto.match;

import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;

@Data
public class MatchDecisionDto {
    private Long matchId;
    private MatchStatus decision;
    private Long userId;
    private String rejectionReason;
}
