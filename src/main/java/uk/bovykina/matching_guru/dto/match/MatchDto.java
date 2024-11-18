package uk.bovykina.matching_guru.dto.match;

import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;

@Data
public class MatchDto {
    private Long id;
    private Long mentorId;
    private Long menteeId;
    private MatchStatus status;
}