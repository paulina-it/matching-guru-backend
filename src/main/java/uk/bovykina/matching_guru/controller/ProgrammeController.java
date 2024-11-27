package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.programme.ProgrammeCreateDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeUpdateDto;
import uk.bovykina.matching_guru.dto.user.UserResponseDto;
import uk.bovykina.matching_guru.entity.enums.UserRole;
import uk.bovykina.matching_guru.exception.UserNotFoundException;
import uk.bovykina.matching_guru.service.ProgrammeService;
import uk.bovykina.matching_guru.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/programmes")
@RequiredArgsConstructor
public class ProgrammeController {

    private final ProgrammeService programmeService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createProgramme(@RequestBody ProgrammeCreateDto programmeCreateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = authentication.getName();
        try {
            UserResponseDto userDto = userService.getUserByEmail(email);

            if (!UserRole.ADMIN.equals(userDto.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: User is not an admin");
            }

            ProgrammeDto programme = programmeService.createProgramme(programmeCreateDto);
            return ResponseEntity.ok(programme);

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating programme");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgrammeDto> getProgrammeById(@PathVariable Long id) {
        try {
            ProgrammeDto programme = programmeService.getProgrammeById(id);
            return ResponseEntity.ok(programme);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @GetMapping
    public ResponseEntity<List<ProgrammeDto>> getAllProgrammes() {
        List<ProgrammeDto> programmes = programmeService.getAllProgrammes();
        return ResponseEntity.ok(programmes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgrammeDto> updateProgramme(
            @PathVariable Long id,
            @RequestBody ProgrammeUpdateDto programmeUpdateDto) {
        ProgrammeDto updatedProgramme = programmeService.updateProgramme(id, programmeUpdateDto);
        return ResponseEntity.ok(updatedProgramme);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgramme(@PathVariable Long id) {
        programmeService.deleteProgramme(id);
        return ResponseEntity.noContent().build();
    }
}
