package uk.bovykina.matching_guru.dto.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.entity.enums.CommunicationStatus;
import uk.bovykina.matching_guru.entity.enums.CommunicationType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSummaryDto {
    private Long matchId;
    private boolean isMentor;
    private String matchWithName;
    private String matchWithEmail;
    private double compatibilityScore;
    private MatchStatus status;
    private Long programmeYearId;
    private String programmeName;
    private String academicYear;
    private boolean feedbackSubmitted;
    private LocalDateTime lastInteractionDate;
    private CommunicationStatus lastInteractionStatus;
    private CommunicationType lastInteractionType;
}