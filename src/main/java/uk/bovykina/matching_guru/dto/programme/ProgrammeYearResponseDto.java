package uk.bovykina.matching_guru.dto.programme;


import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.AlgorithmType;
import uk.bovykina.matching_guru.entity.enums.MatchApprovalType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ProgrammeYearResponseDto {
    private Long id;
    private Long programmeId;
    private String programmeName;
    private String programmeDescription;
    private String academicYear;
    private String contactEmail;
    private Boolean isActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime signupOpenDate;
    private LocalDateTime signupCloseDate;
    private String joinCode;
    private AlgorithmType preferredAlgorithm;
    private List<MatchingCriteriaDto> matchingCriteria;
    private Integer participantCount;
    private Boolean initialMatchingIsDone;
    private MatchApprovalType matchApprovalType;
    private Integer approvalThreshold;
    private Integer unmatchedCount;
    private Boolean strictAcademicStage;
    private Boolean strictCourseGroup;
    private LocalDateTime surveyOpenDate;
    private LocalDateTime surveyCloseDate;
    private String feedbackConfirmationCode;
    private String surveyUrl;
    private boolean isSurveyOpen;
    private boolean isSignupOpen;
    private boolean isCurrentlyRunning;
    private String certificateTemplateUrl;
}
