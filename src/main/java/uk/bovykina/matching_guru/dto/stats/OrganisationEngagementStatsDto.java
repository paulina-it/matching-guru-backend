package uk.bovykina.matching_guru.dto.stats;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrganisationEngagementStatsDto {
    private String organisation;
    private List<ProgrammeEngagementStatsDto> programmes;
}
