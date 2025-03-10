package uk.bovykina.matching_guru.dto.programme;


import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.AlgorithmType;
import uk.bovykina.matching_guru.entity.enums.MatchApprovalType;

import java.util.List;

@Data
public class ProgrammeYearCreateDto {
    private Long programmeId;
    private String academicYear;
    private String customSettings;
    private AlgorithmType preferredAlgorithm;
    private List<MatchingCriteriaDto> matchingCriteria;
    private MatchApprovalType matchApprovalType;
    private Integer approvalThreshold;
}