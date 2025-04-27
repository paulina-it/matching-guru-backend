package uk.bovykina.matching_guru.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.participant.FeedbackSubmissionDto;
import uk.bovykina.matching_guru.dto.survey.EndSurveyResponseCreateDto;
import uk.bovykina.matching_guru.dto.survey.EndSurveyResponseDto;
import uk.bovykina.matching_guru.entity.EndSurveyResponse;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.mapper.EndSurveyMapper;
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
    private final EndSurveyMapper endSurveyMapper;

    /**
     * Handles feedback code submission and stores response metadata.
     */
    public boolean handleFeedbackSubmission(FeedbackSubmissionDto dto) {
        log.info("Handling feedback for participantId={} in programmeYearId={}", dto.getParticipantId(), dto.getProgrammeYearId());

        ProgrammeYear programmeYear = programmeYearRepository.findById(dto.getProgrammeYearId())
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found."));

        if (!dto.getCode().equals(programmeYear.getFeedbackConfirmationCode())) {
            log.warn("Invalid feedback code: received {}, expected {}", dto.getCode(), programmeYear.getFeedbackConfirmationCode());
            return false;
        }

        ParticipantInProgrammeYear participant = participantRepository
                .findByIdAndProgrammeYearId(dto.getParticipantId(), dto.getProgrammeYearId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found."));

        EndSurveyResponse response = new EndSurveyResponse();
        response.setParticipantInProgramme(participant);
        response.setProgrammeYear(programmeYear);
        response.setCompletedAt(LocalDateTime.now());
        response.setFeedbackConfirmationCode(dto.getCode());
        // Optional: response.setResponseData(dto.getResponseData());

        endSurveyResponseRepository.save(response);
        log.info("Feedback saved for participantId={}", participant.getId());

        return true;
    }

    /**
     * Retrieves all survey responses for a participant.
     */
    public List<EndSurveyResponseDto> getResponsesByParticipant(Long participantId) {
        log.info("Fetching survey responses for participant ID: {}", participantId);

        return endSurveyResponseRepository.findByParticipantInProgrammeId(participantId).stream()
                .map(endSurveyMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all end survey responses.
     */
    public List<EndSurveyResponseDto> getAllEndSurveyResponses() {
        log.info("Fetching all end survey responses");

        return endSurveyResponseRepository.findAll().stream()
                .map(endSurveyMapper::toDto)
                .collect(Collectors.toList());
    }
}
