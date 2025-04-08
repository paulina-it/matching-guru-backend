package uk.bovykina.matching_guru.dto.stats;

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
    private List<ProgrammeStatsDto> programmes;
}