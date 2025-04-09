package uk.bovykina.matching_guru.dto.programme;

import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.AlgorithmType;
import uk.bovykina.matching_guru.entity.enums.MatchApprovalType;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProgrammeYearUpdateDto {
    private String academicYear;
    private String customSettings;
    private AlgorithmType preferredAlgorithm;
    private Boolean isActive;
    private List<MatchingCriteriaDto> matchingCriteria;
    private MatchApprovalType matchApprovalType;
    private Integer approvalThreshold;
    private Boolean strictAcademicStage;
    private Boolean strictCourseGroup;
    private LocalDateTime surveyOpenDate;
    private LocalDateTime surveyCloseDate;
    private String surveyUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime signupOpenDate;
    private LocalDateTime signupCloseDate;
    private String certificateTemplateUrl;
}