package uk.bovykina.matching_guru.dto.stats;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProgrammeYearStatsDto {
    private String academicYear;
    private int participants;
    private int matches;
    private double rate;
}