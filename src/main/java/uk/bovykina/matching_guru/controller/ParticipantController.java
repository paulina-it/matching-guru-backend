package uk.bovykina.matching_guru.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.participant.ParticipantCreateDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantResponseDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantUpdateDto;
import uk.bovykina.matching_guru.service.ParticipantService;

import java.util.List;

@RestController
@RequestMapping("/participants")
@RequiredArgsConstructor
@Slf4j
public class ParticipantController {

    private final ParticipantService participantService;

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
