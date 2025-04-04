package uk.bovykina.matching_guru.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.participant.FeedbackSubmissionDto;
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

    @Transactional
    public boolean handleFeedbackSubmission(FeedbackSubmissionDto dto) {
        log.info("📝 Handling feedback for participantId={} in programmeYearId={}", dto.getParticipantId(), dto.getProgrammeYearId());

        ProgrammeYear programmeYear = programmeYearRepository.findById(dto.getProgrammeYearId())
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found."));

        if (!dto.getCode().equals(programmeYear.getFeedbackConfirmationCode())) {
            log.warn("❌ Invalid feedback code: received {}, expected {}", dto.getCode(), programmeYear.getFeedbackConfirmationCode());
            return dto.getCode().equals(programmeYear.getFeedbackConfirmationCode());
        }

        ParticipantInProgrammeYear participant = participantRepository
                .findByIdAndProgrammeYearId(dto.getParticipantId(), dto.getProgrammeYearId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found."));
        log.info("Participant found: {}", participant.getRole());
        EndSurveyResponse response = new EndSurveyResponse();
        response.setParticipantInProgramme(participant);
        response.setProgrammeYear(programmeYear);
        response.setCompletedAt(LocalDateTime.now());
        response.setFeedbackConfirmationCode(dto.getCode());
//        response.setResponseData(dto.getResponseData());

        endSurveyResponseRepository.save(response);

        log.info("✅ Feedback saved for participantId={}", participant.getId());
        return dto.getCode().equals(programmeYear.getFeedbackConfirmationCode());
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
//            dto.setResponseData(surveyResponse.getResponseData());
            dto.setCompletedAt(surveyResponse.getCompletedAt());
            return dto;
        }

        public EndSurveyResponse toEntity(EndSurveyResponseCreateDto dto, ParticipantInProgrammeYear participant, ProgrammeYear programmeYear) {
            log.info("Mapping EndSurveyResponseCreateDto to EndSurveyResponse for participant ID: {}", participant.getId());

            EndSurveyResponse surveyResponse = new EndSurveyResponse();
            surveyResponse.setParticipantInProgramme(participant);
            surveyResponse.setProgrammeYear(programmeYear);
//            surveyResponse.setResponseData(dto.getResponseData());
            return surveyResponse;
        }
    }
}
