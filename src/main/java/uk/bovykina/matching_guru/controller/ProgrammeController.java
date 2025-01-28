package uk.bovykina.matching_guru.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.programme.ProgrammeCreateDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeUpdateDto;
import uk.bovykina.matching_guru.dto.user.UserResponseDto;
import uk.bovykina.matching_guru.entity.enums.UserRole;
import uk.bovykina.matching_guru.exception.UserNotFoundException;
import uk.bovykina.matching_guru.service.ProgrammeService;
import uk.bovykina.matching_guru.service.UserService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/programmes")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ProgrammeController {

    private final ProgrammeService programmeService;
    private final UserService userService;

    private boolean isUserAdmin(UserResponseDto userDto) {
        return UserRole.ADMIN.equals(userDto.getRole());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProgramme(@Valid @RequestBody ProgrammeCreateDto programmeCreateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = authentication.getName();
        try {
            UserResponseDto userDto = userService.getUserByEmail(email);

            if (!isUserAdmin(userDto)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: User is not an admin");
            }

            ProgrammeDto programme = programmeService.createProgramme(programmeCreateDto);
            log.info("Programme created successfully: {}", programme);
            return ResponseEntity.status(HttpStatus.CREATED).body(programme);

        } catch (UserNotFoundException e) {
            log.error("User not found: {}", email, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for creating programme: {}", programmeCreateDto, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error while creating programme", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating programme");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgrammeDto> getProgrammeById(@PathVariable Long id) {
        try {
            ProgrammeDto programme = programmeService.getProgrammeById(id);
            return ResponseEntity.ok(programme);
        } catch (IllegalArgumentException e) {
            log.error("Programme not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<ProgrammeDto>> getAllProgrammes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<ProgrammeDto> programmes = programmeService.getAllProgrammes();
            return ResponseEntity.ok(programmes);
        } catch (Exception e) {
            log.error("Error while fetching all programmes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/organisation/{organisationId}")
    public ResponseEntity<List<ProgrammeDto>> getProgrammesByOrganisation(@PathVariable Long organisationId) {
        try {
            List<ProgrammeDto> programmes = programmeService.getProgrammesByOrganisation(organisationId);
            return ResponseEntity.ok(programmes);
        } catch (IllegalArgumentException e) {
            log.error("Invalid organisation id: {}", organisationId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @GetMapping("/organisation/{organisationId}/active")
    public ResponseEntity<List<ProgrammeDto>> getActiveProgrammesByOrganisation(@PathVariable Long organisationId) {
        try {
            List<ProgrammeDto> programmes = programmeService.getActiveProgrammesByOrganisation(organisationId);
            return ResponseEntity.ok(programmes);
        } catch (IllegalArgumentException e) {
            log.error("Invalid organisation id: {}", organisationId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProgrammeDto>> getProgrammesByUserId(@PathVariable Long userId) {
        try {
            List<ProgrammeDto> programmes = programmeService.getProgrammesByUserId(userId);
            return ResponseEntity.ok(programmes);
        } catch (IllegalArgumentException e) {
            log.error("User not found: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        } catch (Exception e) {
            log.error("Error fetching programmes for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProgramme(
            @PathVariable Long id,
            @Valid @RequestBody ProgrammeUpdateDto programmeUpdateDto) {
        try {
            ProgrammeDto updatedProgramme = programmeService.updateProgramme(id, programmeUpdateDto);
            log.info("Programme updated successfully: {}", updatedProgramme);
            return ResponseEntity.ok(updatedProgramme);
        } catch (IllegalArgumentException e) {
            log.error("Programme not found for update with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error while updating programme with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the programme");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgramme(@PathVariable Long id) {
        try {
            programmeService.deleteProgramme(id);
            log.info("Programme deleted with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Programme not found for deletion with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
