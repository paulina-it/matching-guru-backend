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
    private List<ProgrammeYearMatchStatsDto> years;
}
