package uk.bovykina.matching_guru.dto.stats;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProgrammeEngagementStatsDto {
    private String programmeName;
    private List<ProgrammeYearEngagementStatsDto> years;
}
