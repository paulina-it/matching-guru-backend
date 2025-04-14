package uk.bovykina.matching_guru.dto.programme;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.AlgorithmType;
import uk.bovykina.matching_guru.entity.enums.MatchApprovalType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProgrammeYearDto {
    private Long id;
    private String academicYear;
    private Boolean isActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime signupOpenDate;
    private LocalDateTime signupCloseDate;
    private AlgorithmType preferredAlgorithm;
    private String joinCode;
    private String surveyUrl;
    private Boolean strictAcademicStage;
    private Boolean strictCourseGroup;
    private String certificateTemplateUrl;
    private Long programmeId;
    private String programmeName;
    private List<MatchingCriteriaDto> matchingCriteria;
    private MatchApprovalType matchApprovalType;
    private Integer approvalThreshold;
}
