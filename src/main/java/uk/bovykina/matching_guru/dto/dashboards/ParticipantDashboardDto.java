package uk.bovykina.matching_guru.dto.dashboards;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.bovykina.matching_guru.dto.match.DetailedMatchResponseDto;
import uk.bovykina.matching_guru.dto.match.MatchSummaryDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDashboardDto {
    private String participantName;
    private String organisationName;

    private List<ProgrammeParticipationSummaryDto> activeParticipations;
    private List<MatchSummaryDto> matches;

    private boolean hasUnconfirmedMatches;
    private boolean hasFeedbackPending;
    private boolean hasOverdueInteractions;

    private LocalDateTime lastInteraction;
    private LocalDateTime nextSuggestedMeetingDate;
    private String suggestedMeetingDay;

    private LocalDateTime lastUpdated;
}
