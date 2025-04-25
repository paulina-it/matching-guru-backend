package uk.bovykina.matching_guru.dto.stats.demographics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationDemographicStatsDto {
    private String organisation;
    private long   totalParticipants;
    private List<CourseBreakdownDto> courseDistribution;
    private List<StageBreakdownDto>  stageDistribution;
    private List<CourseGroupBreakdownDto> courseGroupDistribution;
    private List<ProgrammeDemoDto>   programmes;
}