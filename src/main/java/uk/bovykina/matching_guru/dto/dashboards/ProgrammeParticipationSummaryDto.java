package uk.bovykina.matching_guru.dto.dashboards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgrammeParticipationSummaryDto {
    private Long programmeYearId;
    private Long programmeId;
    private Long participantId;
    private String programmeName;
    private String academicYear;
    private ParticipantRole role;
    private boolean isMatched;
    private boolean feedbackSubmitted;
    private String surveyUrl;
    private LocalDateTime surveyCloseDate;
}
