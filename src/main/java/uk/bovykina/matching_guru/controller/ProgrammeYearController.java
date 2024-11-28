package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.programme.*;
import uk.bovykina.matching_guru.service.ProgrammeYearService;

import java.util.List;

@RestController
@RequestMapping("/api/programme-years")
@RequiredArgsConstructor
public class ProgrammeYearController {

    private final ProgrammeYearService programmeYearService;

    /**
     * Create a new ProgrammeYear with optional matching criteria.
     */
    @PostMapping
    public ResponseEntity<ProgrammeYearResponseDto> createProgrammeYear(
            @RequestBody ProgrammeYearCreateDto createDto) {
        ProgrammeYearResponseDto response = programmeYearService.createProgrammeYear(createDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve a ProgrammeYear by its ID, including its matching criteria.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProgrammeYearResponseDto> getProgrammeYear(@PathVariable Long id) {
        ProgrammeYearResponseDto response = programmeYearService.getProgrammeYear(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve all ProgrammeYears.
     */
    @GetMapping
    public ResponseEntity<List<ProgrammeYearResponseDto>> getAllProgrammeYears() {
        List<ProgrammeYearResponseDto> response = programmeYearService.getAllProgrammeYears();
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing ProgrammeYear and its matching criteria.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProgrammeYearResponseDto> updateProgrammeYear(
            @PathVariable Long id,
            @RequestBody ProgrammeYearUpdateDto updateDto) {
        ProgrammeYearResponseDto response = programmeYearService.updateProgrammeYear(id, updateDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a ProgrammeYear by its ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgrammeYear(@PathVariable Long id) {
        programmeYearService.deleteProgrammeYear(id);
        return ResponseEntity.noContent().build();
    }
}
