package uk.bovykina.matching_guru.dto.stats;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProgrammeYearMatchStatsDto {
    private String academicYear;
    private int participants;
    private int matches;
    private double rate;

    private int accepted;
    private int rejected;
    private int pending;
    private int acceptedByOne;
    private int acceptedByBoth;

    private double acceptRate;
    private double rejectRate;
}