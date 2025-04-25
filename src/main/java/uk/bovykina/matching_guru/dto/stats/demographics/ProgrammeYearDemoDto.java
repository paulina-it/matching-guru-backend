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
public class ProgrammeYearDemoDto {
    private String academicYear;
    private List<CourseBreakdownDto> courseDistribution;
    private List<StageBreakdownDto>  stageDistribution;
    private long                    totalParticipants;
    private List<CourseGroupBreakdownDto> courseGroupDistribution;
}
