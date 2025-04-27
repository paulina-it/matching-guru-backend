package uk.bovykina.matching_guru.mapper;

import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.dto.programme.MatchingCriteriaDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearResponseDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearCreateDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearUpdateDto;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.ProgrammeMatchingCriteria;
import uk.bovykina.matching_guru.entity.ProgrammeYear;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProgrammeYearMapper {

    public ProgrammeYearResponseDto toResponseDto(
            ProgrammeYear programmeYear,
            int participantCount,
            int matchedCount,
            boolean initialMatchingIsDone,
            List<ProgrammeMatchingCriteria> matchingCriteria
    ) {
        ProgrammeYearResponseDto dto = new ProgrammeYearResponseDto();
        dto.setId(programmeYear.getId());
        dto.setProgrammeId(programmeYear.getProgramme().getId());
        dto.setProgrammeName(programmeYear.getProgramme().getName());
        dto.setProgrammeDescription(programmeYear.getProgramme().getDescription());
        dto.setContactEmail(programmeYear.getProgramme().getContactEmail());
        dto.setAcademicYear(programmeYear.getAcademicYear());
        dto.setIsActive(programmeYear.getIsActive());
        dto.setJoinCode(programmeYear.getJoinCode());
        dto.setPreferredAlgorithm(programmeYear.getPreferredAlgorithm());
        dto.setMatchApprovalType(programmeYear.getMatchApprovalType());
        dto.setApprovalThreshold(programmeYear.getApprovalThreshold());
        dto.setStrictAcademicStage(programmeYear.getStrictAcademicStage());
        dto.setStrictCourseGroup(programmeYear.getStrictCourseGroup());
        dto.setSurveyOpenDate(programmeYear.getSurveyOpenDate());
        dto.setSurveyCloseDate(programmeYear.getSurveyCloseDate());
        dto.setFeedbackConfirmationCode(programmeYear.getFeedbackConfirmationCode());
        dto.setSurveyUrl(programmeYear.getSurveyUrl());
        dto.setStartDate(programmeYear.getStartDate());
        dto.setEndDate(programmeYear.getEndDate());
        dto.setSignupOpenDate(programmeYear.getSignupOpenDate());
        dto.setSignupCloseDate(programmeYear.getSignupCloseDate());
        dto.setSurveyOpen(programmeYear.isSurveyOpen());
        dto.setSignupOpen(programmeYear.isSignupOpen());
        dto.setCurrentlyRunning(programmeYear.isCurrentlyRunning());
        dto.setCertificateTemplateUrl(programmeYear.getCertificateTemplateUrl());
        dto.setParticipantCount(participantCount);
        dto.setUnmatchedCount(participantCount - matchedCount);
        dto.setInitialMatchingIsDone(initialMatchingIsDone);

        dto.setMatchingCriteria(
                matchingCriteria.stream().map(this::toMatchingCriteriaDto).collect(Collectors.toList())
        );

        return dto;
    }

    public ProgrammeYearDto toDto(ProgrammeYear programmeYear) {
        ProgrammeYearDto dto = new ProgrammeYearDto();
        dto.setId(programmeYear.getId());
        dto.setAcademicYear(programmeYear.getAcademicYear());
        dto.setIsActive(programmeYear.getIsActive());
        dto.setStartDate(programmeYear.getStartDate());
        dto.setEndDate(programmeYear.getEndDate());
        dto.setSignupOpenDate(programmeYear.getSignupOpenDate());
        dto.setSignupCloseDate(programmeYear.getSignupCloseDate());
        dto.setPreferredAlgorithm(programmeYear.getPreferredAlgorithm());
        dto.setJoinCode(programmeYear.getJoinCode());
        dto.setSurveyUrl(programmeYear.getSurveyUrl());
        dto.setStrictAcademicStage(programmeYear.getStrictAcademicStage());
        dto.setStrictCourseGroup(programmeYear.getStrictCourseGroup());
        dto.setCertificateTemplateUrl(programmeYear.getCertificateTemplateUrl());
        dto.setMatchApprovalType(programmeYear.getMatchApprovalType());
        dto.setApprovalThreshold(programmeYear.getApprovalThreshold());

        if (programmeYear.getProgramme() != null) {
            dto.setProgrammeId(programmeYear.getProgramme().getId());
            dto.setProgrammeName(programmeYear.getProgramme().getName());
        }

        dto.setMatchingCriteria(
                programmeYear.getMatchingCriteria().stream()
                        .map(this::toMatchingCriteriaDto)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    public ProgrammeYear toEntity(ProgrammeYearCreateDto dto, Programme programme, String joinCode) {
        ProgrammeYear py = new ProgrammeYear();
        py.setProgramme(programme);
        py.setAcademicYear(dto.getAcademicYear());
        py.setIsActive(true);
        py.setJoinCode(joinCode);
        py.setPreferredAlgorithm(dto.getPreferredAlgorithm());
        py.setFeedbackConfirmationCode(py.generateFeedbackConfirmationCode());
        py.setStrictAcademicStage(dto.getStrictAcademicStage());
        py.setStrictCourseGroup(dto.getStrictCourseGroup());
        py.setSurveyOpenDate(dto.getSurveyOpenDate());
        py.setSurveyCloseDate(dto.getSurveyCloseDate());
        py.setSurveyUrl(dto.getSurveyUrl());
        py.setStartDate(dto.getStartDate());
        py.setEndDate(dto.getEndDate());
        py.setSignupOpenDate(dto.getSignupOpenDate());
        py.setSignupCloseDate(dto.getSignupCloseDate());
        py.setCertificateTemplateUrl(dto.getCertificateTemplateUrl());
        py.setMatchApprovalType(dto.getMatchApprovalType());
        return py;
    }

    public void updateEntityFromDto(ProgrammeYear py, ProgrammeYearUpdateDto dto) {
        if (dto.getAcademicYear() != null) py.setAcademicYear(dto.getAcademicYear());
        if (dto.getPreferredAlgorithm() != null) py.setPreferredAlgorithm(dto.getPreferredAlgorithm());
        if (dto.getStrictAcademicStage() != null) py.setStrictAcademicStage(dto.getStrictAcademicStage());
        if (dto.getStrictCourseGroup() != null) py.setStrictCourseGroup(dto.getStrictCourseGroup());
        if (dto.getSurveyOpenDate() != null) py.setSurveyOpenDate(dto.getSurveyOpenDate());
        if (dto.getSurveyCloseDate() != null) py.setSurveyCloseDate(dto.getSurveyCloseDate());
        if (dto.getSurveyUrl() != null) py.setSurveyUrl(dto.getSurveyUrl());
        if (dto.getIsActive() != null) py.setIsActive(dto.getIsActive());
        if (dto.getStartDate() != null) py.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) py.setEndDate(dto.getEndDate());
        if (dto.getSignupOpenDate() != null) py.setSignupOpenDate(dto.getSignupOpenDate());
        if (dto.getSignupCloseDate() != null) py.setSignupCloseDate(dto.getSignupCloseDate());
        if (dto.getMatchApprovalType() != null) py.setMatchApprovalType(dto.getMatchApprovalType());
        if (dto.getCertificateTemplateUrl() != null) py.setCertificateTemplateUrl(dto.getCertificateTemplateUrl());
    }

    public ProgrammeMatchingCriteria toEntity(MatchingCriteriaDto dto, ProgrammeYear py) {
        ProgrammeMatchingCriteria entity = new ProgrammeMatchingCriteria();
        entity.setProgrammeYear(py);
        entity.setCriterionType(dto.getCriterionType());
        entity.setWeight(dto.getWeight());
        return entity;
    }

    public MatchingCriteriaDto toMatchingCriteriaDto(ProgrammeMatchingCriteria criterion) {
        MatchingCriteriaDto dto = new MatchingCriteriaDto();
        dto.setCriterionType(criterion.getCriterionType());
        dto.setWeight(criterion.getWeight());
        return dto;
    }
}
