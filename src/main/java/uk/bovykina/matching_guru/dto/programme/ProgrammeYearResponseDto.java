package uk.bovykina.matching_guru.dto.programme;


import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.AlgorithmType;

import java.util.List;

@Getter
@Setter
public class ProgrammeYearResponseDto {
    private Long id;
    private Long programmeId;
    private String programmeName;
    private String academicYear;
    private Boolean isActive;
    private String joinCode;
//    private String customSettings;
    private AlgorithmType preferredAlgorithm;
    private List<MatchingCriteriaDto> matchingCriteria;
    private Integer participantCount;
    private Boolean initialMatchingIsDone;
}
