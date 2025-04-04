package uk.bovykina.matching_guru.dto.dashboards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgrammeParticipationSummaryDto {
    private Long programmeYearId;
    private Long programmeId;
    private String programmeName;
    private String academicYear;
    private ParticipantRole role;
    private boolean isMatched;
    private boolean feedbackSubmitted;
}
