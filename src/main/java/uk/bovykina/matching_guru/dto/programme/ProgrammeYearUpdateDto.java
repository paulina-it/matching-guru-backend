package uk.bovykina.matching_guru.dto.programme;

import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.AlgorithmType;

import java.util.List;

@Data
public class ProgrammeYearUpdateDto {
    private String academicYear;
    private String customSettings;
    private AlgorithmType preferredAlgorithm;
    private boolean isActive;
    private List<MatchingCriteriaDto> matchingCriteria;
}