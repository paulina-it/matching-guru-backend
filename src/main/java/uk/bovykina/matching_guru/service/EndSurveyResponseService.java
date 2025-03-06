package uk.bovykina.matching_guru.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.survey.*;
import uk.bovykina.matching_guru.entity.EndSurveyResponse;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.repository.EndSurveyResponseRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.ProgrammeYearRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EndSurveyResponseService {

    private final EndSurveyResponseRepository endSurveyResponseRepository;
    private final ParticipantRepository participantRepository;
    private final ProgrammeYearRepository programmeYearRepository;
    private final EndSurveyMapper endSurveyMapper = new EndSurveyMapper();

    public EndSurveyResponseDto createEndSurveyResponse(EndSurveyResponseCreateDto dto, String feedbackConfirmationCode) {
        log.info("Processing end survey response for participant ID: {}", dto.getParticipantId());

        ParticipantInProgrammeYear participant = participantRepository.findById(dto.getParticipantId())
                .orElseThrow(() -> {
                    log.error("Participant not found with ID: {}", dto.getParticipantId());
                    return new IllegalArgumentException("Participant not found");
                });

        ProgrammeYear programmeYear = programmeYearRepository.findById(participant.getProgrammeYear().getId())
                .orElseThrow(() -> {
                    log.error("Programme year not found for participant: {}", dto.getParticipantId());
                    return new IllegalArgumentException("Programme year not found");
                });

        if (!programmeYear.getFeedbackConfirmationCode().equals(feedbackConfirmationCode)) {
            log.warn("Invalid feedback confirmation code provided: {}", feedbackConfirmationCode);
            throw new IllegalArgumentException("Invalid feedback confirmation code.");
        }

        EndSurveyResponse surveyResponse = endSurveyMapper.toEntity(dto, participant);
        surveyResponse.setCompletedAt(LocalDateTime.now());
        surveyResponse.setFeedbackConfirmationCode(feedbackConfirmationCode);
        surveyResponse = endSurveyResponseRepository.save(surveyResponse);

        log.info("Successfully processed end survey response for participant ID: {}", dto.getParticipantId());
        return endSurveyMapper.toDto(surveyResponse);
    }

    public List<EndSurveyResponseDto> getResponsesByParticipant(Long participantId) {
        log.info("Fetching survey responses for participant ID: {}", participantId);

        List<EndSurveyResponseDto> responses = endSurveyResponseRepository.findByParticipantInProgrammeId(participantId).stream()
                .map(endSurveyMapper::toDto)
                .collect(Collectors.toList());

        log.info("Found {} responses for participant ID: {}", responses.size(), participantId);
        return responses;
    }

    public List<EndSurveyResponseDto> getAllEndSurveyResponses() {
        log.info("Fetching all end survey responses");

        List<EndSurveyResponseDto> responses = endSurveyResponseRepository.findAll().stream()
                .map(endSurveyMapper::toDto)
                .collect(Collectors.toList());

        log.info("Found {} total survey responses", responses.size());
        return responses;
    }

    private static class EndSurveyMapper {

        public EndSurveyResponseDto toDto(EndSurveyResponse surveyResponse) {
            log.info("Mapping EndSurveyResponse to DTO for ID: {}", surveyResponse.getId());

            EndSurveyResponseDto dto = new EndSurveyResponseDto();
            dto.setId(surveyResponse.getId());
            dto.setParticipantInProgrammeId(surveyResponse.getParticipantInProgramme().getId());
            dto.setResponseData(surveyResponse.getResponseData());
            dto.setCompletedAt(surveyResponse.getCompletedAt());
            return dto;
        }

        public EndSurveyResponse toEntity(EndSurveyResponseCreateDto dto, ParticipantInProgrammeYear participant) {
            log.info("Mapping EndSurveyResponseCreateDto to EndSurveyResponse for participant ID: {}", participant.getId());

            EndSurveyResponse surveyResponse = new EndSurveyResponse();
            surveyResponse.setParticipantInProgramme(participant);
            surveyResponse.setResponseData(dto.getResponseData());
            return surveyResponse;
        }
    }
}
