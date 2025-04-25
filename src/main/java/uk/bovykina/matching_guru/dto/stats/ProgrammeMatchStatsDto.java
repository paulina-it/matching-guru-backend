package uk.bovykina.matching_guru.dto.stats;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ProgrammeMatchStatsDto {
    private String programmeName;
    private int totalParticipants;
    private int totalMatches;
    private double matchRate;
    private int accepted;
    private int rejected;
    private int pending;
    private double acceptRate;
    private double rejectRate;
    private int acceptedByBoth;
    private int acceptedByOne;
    private List<ProgrammeYearMatchStatsDto> years;
}
