package uk.bovykina.matching_guru.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.participant.FeedbackSubmissionDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantCreateDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantResponseDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantUpdateDto;
import uk.bovykina.matching_guru.entity.EndSurveyResponse;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.service.EndSurveyResponseService;
import uk.bovykina.matching_guru.service.ParticipantService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/participants")
@RequiredArgsConstructor
@Slf4j
public class ParticipantController {

    private final ParticipantService participantService;
    private final EndSurveyResponseService endSurveyResponseService;

    @PostMapping("/create")
    public ResponseEntity<?> createParticipant(@Valid @RequestBody ParticipantCreateDto participantCreateDto) {
        try {
            ParticipantResponseDto participant = participantService.createParticipant(participantCreateDto);
            log.info("Participant created successfully: {}", participant);
            return ResponseEntity.status(HttpStatus.CREATED).body(participant);
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for creating participant: {}", participantCreateDto, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error while creating participant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating participant");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getParticipant(@PathVariable Long id) {
        try {
            ParticipantResponseDto participant = participantService.getParticipant(id);
            return ResponseEntity.ok(participant);
        } catch (IllegalArgumentException e) {
            log.error("Participant not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error while fetching participant with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching participant");
        }
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<?> getParticipantInfoByUserId(@PathVariable Long id) {
        try {
            Object response = participantService.getParticipantInfoByUserId(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Participant not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error while fetching participant with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching participant");
        }
    }


    @GetMapping("/user/{id}")
    public ResponseEntity<?> getParticipantByUserId(@PathVariable Long id) {
        try {
            ParticipantResponseDto participant = participantService.getParticipantByUserId(id);
            return ResponseEntity.ok(participant);
        } catch (IllegalArgumentException e) {
            log.error("Participant not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error while fetching participant with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching participant");
        }
    }
    @GetMapping
    public ResponseEntity<List<ParticipantResponseDto>> getAllParticipants() {
        try {
            List<ParticipantResponseDto> participants = participantService.getAllParticipants();
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            log.error("Error while fetching all participants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateParticipant(
            @PathVariable Long id,
            @Valid @RequestBody ParticipantUpdateDto participantUpdateDto) {
        try {
            ParticipantResponseDto updatedParticipant = participantService.updateParticipant(id, participantUpdateDto);
            log.info("Participant updated successfully: {}", updatedParticipant);
            return ResponseEntity.ok(updatedParticipant);
        } catch (IllegalArgumentException e) {
            log.error("Participant not found for update with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error while updating participant with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating participant");
        }
    }

    @GetMapping("/programme-year/{programmeYearId}")
    public ResponseEntity<?> getParticipantsByProgrammeYearId(
            @PathVariable Long programmeYearId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "userName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String role
    ) {
        Page<ParticipantResponseDto> participants = participantService.getParticipantsByProgrammeYearId(
                programmeYearId, page, size, search, sortBy, sortOrder, role
        );
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/programme-year/detailed/{programmeYearId}")
    public ResponseEntity<List<ParticipantResponseDto>> getDetailedParticipantsByProgrammeYearId(
            @PathVariable Long programmeYearId
    ) {
        List<ParticipantResponseDto> participants = participantService.getDetailedParticipantsByProgrammeYearId(programmeYearId);
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/info/{userId}/programmeYear/{programmeYearId}")
    public ResponseEntity<?> getParticipantInfoByUserIdAndProgrammeYearId(
            @PathVariable Long userId,
            @PathVariable Long programmeYearId
    ) {
        try {
            Object participantInfo = participantService.getParticipantInfoByUserIdAndProgrammeYearId(userId, programmeYearId);

            if (participantInfo instanceof List) {
                return ResponseEntity.ok().body((List<?>) participantInfo);
            } else {
                return ResponseEntity.ok(participantInfo);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Bad request for userId={} and programmeYearId={}: {}", userId, programmeYearId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error fetching participant info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch participant info");
        }
    }

    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackSubmissionDto dto) {
        try {
            endSurveyResponseService.handleFeedbackSubmission(dto);
            return ResponseEntity.ok("Feedback recorded.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error during feedback submission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to submit feedback.");
        }
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteParticipant(@PathVariable Long id) {
//        try {
//            participantService.deleteParticipant(id);
//            log.info("Participant deleted with id: {}", id);
//            return ResponseEntity.noContent().build();
//        } catch (IllegalArgumentException e) {
//            log.error("Participant not found for deletion with id: {}", id, e);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        } catch (Exception e) {
//            log.error("Error while deleting participant with id: {}", id, e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting participant");
//        }
//    }
}
