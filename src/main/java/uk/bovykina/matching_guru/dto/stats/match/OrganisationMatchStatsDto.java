package uk.bovykina.matching_guru.dto.stats.match;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrganisationMatchStatsDto {
    private String organisation;
    private int totalParticipants;
    private int totalMatches;
    private double matchRatePercent;
    private int totalAccepted;
    private int totalRejected;
    private double overallAcceptRate;
    private double overallRejectRate;
    private int acceptedByBoth;
    private int acceptedByOne;
    private int pending;
    private int rejected;
    private List<ProgrammeMatchStatsDto> programmes;
}