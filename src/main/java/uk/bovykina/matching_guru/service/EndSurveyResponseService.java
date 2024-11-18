package uk.bovykina.matching_guru.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.survey.*;
import uk.bovykina.matching_guru.entity.EndSurveyResponse;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.repository.EndSurveyResponseRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EndSurveyResponseService {

    private final EndSurveyResponseRepository endSurveyResponseRepository;
    private final ParticipantRepository participantRepository;
    private final EndSurveyMapper endSurveyMapper = new EndSurveyMapper();


    public EndSurveyResponseDto createEndSurveyResponse(EndSurveyResponseCreateDto dto) {
        ParticipantInProgrammeYear participant = participantRepository.findById(dto.getParticipantId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        EndSurveyResponse surveyResponse = endSurveyMapper.toEntity(dto, participant);
        surveyResponse.setCompletedAt(LocalDateTime.now());
        surveyResponse = endSurveyResponseRepository.save(surveyResponse);
        return endSurveyMapper.toDto(surveyResponse);
    }

    public List<EndSurveyResponseDto> getResponsesByParticipant(Long participantId) {
        return endSurveyResponseRepository.findByParticipantInProgrammeId(participantId).stream()
                .map(endSurveyMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<EndSurveyResponseDto> getAllEndSurveyResponses() {
        return endSurveyResponseRepository.findAll().stream()
                .map(endSurveyMapper::toDto)
                .collect(Collectors.toList());
    }


    private static class EndSurveyMapper {
        public EndSurveyResponseDto toDto(EndSurveyResponse surveyResponse) {
            EndSurveyResponseDto dto = new EndSurveyResponseDto();
            dto.setId(surveyResponse.getId());
            dto.setParticipantInProgrammeId(surveyResponse.getParticipantInProgramme().getId());
            dto.setResponseData(surveyResponse.getResponseData());
            dto.setCompletedAt(surveyResponse.getCompletedAt());
            return dto;
        }

        public EndSurveyResponse toEntity(EndSurveyResponseCreateDto dto, ParticipantInProgrammeYear participant) {
            EndSurveyResponse surveyResponse = new EndSurveyResponse();
            surveyResponse.setParticipantInProgramme(participant);
            surveyResponse.setResponseData(dto.getResponseData());
            return surveyResponse;
        }
    }
}