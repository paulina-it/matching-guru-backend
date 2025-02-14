package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.algorithms.GaleShapleyService;
import uk.bovykina.matching_guru.dto.match.MatchResponseDto;
import uk.bovykina.matching_guru.service.MatchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {
    private final GaleShapleyService galeShapleyService;
    private final MatchService matchService;

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.ACCEPTED)  // Returns 202 Accepted (Async Process)
    public void matchParticipants(@RequestParam Long programmeId, @RequestParam boolean isInitial) {
        galeShapleyService.matchParticipants(programmeId, isInitial);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Page<MatchResponseDto>> getMatchesByProgrammeYearId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Page<MatchResponseDto> matchesPage = matchService.getMatchesByProgrammeYearId(id, PageRequest.of(page, size));
        return ResponseEntity.ok(matchesPage);
    }
}
