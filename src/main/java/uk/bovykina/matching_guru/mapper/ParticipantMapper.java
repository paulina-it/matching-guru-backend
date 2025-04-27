package uk.bovykina.matching_guru.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.dto.participant.ParticipantCreateDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantResponseDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantUpdateDto;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.repository.EndSurveyResponseRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;

@Component
@RequiredArgsConstructor
public class ParticipantMapper {

    private final EndSurveyResponseRepository endSurveyResponseRepository;
    private final CourseRepository courseRepository;

    public ParticipantResponseDto toDto(ParticipantInProgrammeYear participant) {
        User user = participant.getUser();

        ParticipantResponseDto dto = new ParticipantResponseDto();
        dto.setId(participant.getId());
        dto.setUserId(user.getId());
        dto.setUserName(user.getFirstName() + " " + user.getLastName());
        dto.setUserEmail(user.getEmail());
        dto.setUserGender(user.getGender());
        dto.setUserHomeCountry(user.getHomeCountry());
        dto.setUserNationality(user.getNationality());
        dto.setUserPersonalityType(user.getPersonalityType());
        dto.setUserCourseId(user.getCourse() != null ? user.getCourse().getId() : null);
        dto.setUserCourseName(user.getCourse() != null ? user.getCourse().getName() : null);
        dto.setUserLivingArrangement(user.getLivingArrangement());
        dto.setUserDbsCertificate(user.getDbsCertificate());
        dto.setUserDisability(user.getDisability());
        dto.setUserAgeGroup(user.getAgeGroup());

        var programmeYear = participant.getProgrammeYear();
        dto.setProgrammeYearId(programmeYear.getId());
        dto.setProgrammeName(programmeYear.getProgramme().getName());
        dto.setAcademicYear(programmeYear.getAcademicYear());

        dto.setRole(participant.getRole());
        dto.setMenteesNumber(participant.getMenteesNumber());
        dto.setIsMatched(participant.getIsMatched());
        dto.setAcademicStage(participant.getAcademicStage());
        dto.setHadPlacement(participant.getHadPlacement());
        dto.setPlacementDescription(participant.getPlacementDescription());
        dto.setMotivation(participant.getMotivation());
        dto.setWasMatchedLastYear(participant.getWasMatchedLastYear());
        dto.setAvailableDays(participant.getAvailableDays());
        dto.setTimeRange(participant.getTimeRange());
        dto.setMeetingsFrequency(participant.getMeetingsFrequency());
        dto.setSkills(participant.getSkills());
        dto.setGenderPreference(participant.getGenderPreference());

        boolean hasFeedback = endSurveyResponseRepository.existsByParticipantInProgramme(participant);
        dto.setHasSubmittedFeedback(hasFeedback);

        return dto;
    }

    public ParticipantInProgrammeYear toEntity(ParticipantCreateDto dto, User user, ProgrammeYear programmeYear) {
        ParticipantInProgrammeYear participant = new ParticipantInProgrammeYear();
        participant.setUser(user);
        participant.setProgrammeYear(programmeYear);
        participant.setRole(dto.getRole());
        participant.setMenteesNumber(dto.getMenteesNumber() != null ? dto.getMenteesNumber() : 0);
        participant.setIsMatched(dto.getIsMatched() != null ? dto.getIsMatched() : false);
        participant.setAcademicStage(dto.getAcademicStage());
        participant.setHadPlacement(dto.getHadPlacement());
        participant.setPlacementDescription(dto.getPlacementDescription());
        participant.setMotivation(dto.getMotivation());
        participant.setAvailableDays(dto.getAvailableDays());
        participant.setTimeRange(dto.getTimeRange());
        participant.setMeetingsFrequency(dto.getMeetingsFrequency());
        participant.setSkills(dto.getSkills());
        participant.setGenderPreference(dto.getGenderPreference());
        return participant;
    }

    public void updateEntity(ParticipantInProgrammeYear participant, ParticipantUpdateDto dto) {
        if (dto.getRole() != null) participant.setRole(dto.getRole());
        if (dto.getMenteesNumber() != null) participant.setMenteesNumber(dto.getMenteesNumber());
        if (dto.getIsMatched() != null) participant.setIsMatched(dto.getIsMatched());
        if (dto.getAcademicStage() != null) participant.setAcademicStage(dto.getAcademicStage());
        if (dto.getHadPlacement() != null) participant.setHadPlacement(dto.getHadPlacement());
        if (dto.getPlacementDescription() != null) participant.setPlacementDescription(dto.getPlacementDescription());
        if (dto.getMotivation() != null) participant.setMotivation(dto.getMotivation());
        if (dto.getWasMatchedLastYear() != null) participant.setWasMatchedLastYear(dto.getWasMatchedLastYear());
        if (dto.getAvailableDays() != null) participant.setAvailableDays(dto.getAvailableDays());
        if (dto.getTimeRange() != null) participant.setTimeRange(dto.getTimeRange());
        if (dto.getMeetingsFrequency() != null) participant.setMeetingsFrequency(dto.getMeetingsFrequency());
        if (dto.getSkills() != null) participant.setSkills(dto.getSkills());
        if (dto.getGenderPreference() != null) participant.setGenderPreference(dto.getGenderPreference());
    }

    public boolean updateUserFromCreateDto(User user, ParticipantCreateDto dto) {
        boolean updated = false;

        if (dto.getGender() != null && user.getGender() == null) {
            user.setGender(dto.getGender());
            updated = true;
        }
        if (dto.getAgeGroup() != null && user.getAgeGroup() == null) {
            user.setAgeGroup(dto.getAgeGroup());
            updated = true;
        }
        if (dto.getHomeCountry() != null && user.getHomeCountry() == null) {
            user.setHomeCountry(dto.getHomeCountry());
            updated = true;
        }
        if (dto.getNationality() != null && user.getNationality() == null) {
            user.setNationality(dto.getNationality());
            updated = true;
        }
        if (dto.getPersonalityType() != null && !dto.getPersonalityType().equals(user.getPersonalityType())) {
            user.setPersonalityType(dto.getPersonalityType());
            updated = true;
        }
        if (dto.getLivingArrangement() != null && user.getLivingArrangement() == null) {
            user.setLivingArrangement(dto.getLivingArrangement());
            updated = true;
        }
        if (dto.getDisability() != null && user.getDisability() == null) {
            user.setDisability(dto.getDisability());
            updated = true;
        }
        if (dto.getDbsCertificate() != null && user.getDbsCertificate() == null) {
            user.setDbsCertificate(dto.getDbsCertificate());
            updated = true;
        }

        return updated;
    }
}
