package uk.bovykina.matching_guru.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.dto.survey.EndSurveyResponseCreateDto;
import uk.bovykina.matching_guru.dto.survey.EndSurveyResponseDto;
import uk.bovykina.matching_guru.entity.EndSurveyResponse;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;

@Component
@Slf4j
public class EndSurveyMapper {

    public EndSurveyResponseDto toDto(EndSurveyResponse surveyResponse) {
        log.info("Mapping EndSurveyResponse to DTO for ID: {}", surveyResponse.getId());

        EndSurveyResponseDto dto = new EndSurveyResponseDto();
        dto.setId(surveyResponse.getId());
        dto.setParticipantInProgrammeId(surveyResponse.getParticipantInProgramme().getId());
        dto.setCompletedAt(surveyResponse.getCompletedAt());
        // Optional: dto.setResponseData(surveyResponse.getResponseData());
        return dto;
    }

    public EndSurveyResponse toEntity(EndSurveyResponseCreateDto dto,
                                      ParticipantInProgrammeYear participant,
                                      ProgrammeYear programmeYear) {
        log.info("Mapping EndSurveyResponseCreateDto to entity for participant ID: {}", participant.getId());

        EndSurveyResponse response = new EndSurveyResponse();
        response.setParticipantInProgramme(participant);
        response.setProgrammeYear(programmeYear);
        // Optional: response.setResponseData(dto.getResponseData());
        return response;
    }
}
