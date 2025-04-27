package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.match.*;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.service.MatchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    /**
     * Retrieves a basic match by its ID.
     */
    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponseDto> getMatchById(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.getMatchById(matchId));
    }

    /**
     * Retrieves detailed match information by match ID.
     */
    @GetMapping("/detailed/{matchId}")
    public ResponseEntity<DetailedMatchResponseDto> getDetailedMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.getDetailedMatchById(matchId));
    }

    /**
     * Retrieves detailed match information for a participant in a specific programme year.
     */
    @GetMapping("/detailed/participant/{participantId}")
    public ResponseEntity<DetailedMatchResponseDto> getDetailedMatchByParticipantId(
            @PathVariable Long participantId,
            @RequestParam Long programmeYearId) {

        DetailedMatchResponseDto matchDto = matchService.getDetailedMatchByParticipantId(participantId, programmeYearId);
        return ResponseEntity.ok(matchDto);
    }

    /**
     * Retrieves a paginated list of matches for a programme year (admin only).
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/programmeYear/{programmeYearId}")
    public ResponseEntity<Page<MatchResponseDto>> getMatchesByProgrammeYear(
            @PathVariable Long programmeYearId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Page<MatchResponseDto> matchesPage = matchService.getMatchesByProgrammeYearId(programmeYearId, PageRequest.of(page, size));
        return ResponseEntity.ok(matchesPage);
    }

    /**
     * Updates the status of multiple matches.
     */
    @PatchMapping("/update-status")
    public ResponseEntity<String> updateMatchStatus(@RequestBody MatchStatusUpdateDto request) {
        if (request.getMatchIds() == null || request.getMatchIds().isEmpty()) {
            return ResponseEntity.badRequest().body("Match IDs must be provided.");
        }

        matchService.updateMatchStatus(request.getMatchIds(), request);
        return ResponseEntity.ok("Matches updated successfully");
    }

    /**
     * Searches matches by query, status, and sort options.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CoordinatorMatchDto>> searchMatches(
            @RequestParam Long programmeYearId,
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false) MatchStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Page<CoordinatorMatchDto> matches = matchService.searchMatches(
                programmeYearId, query, status, page, size, sortBy, sortOrder
        );
        return ResponseEntity.ok(matches);
    }

    /**
     * Processes a participant's decision on a match.
     */
    @PatchMapping("/decision")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<String> decideOnMatch(@RequestBody MatchDecisionDto request) {
        matchService.processParticipantDecision(request);
        return ResponseEntity.ok("Match decision processed successfully");
    }

    /**
     * Deletes all matches for a given programme year (admin only).
     */
    @DeleteMapping("/programmeYear/{programmeYearId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteMatchesByProgrammeYear(@PathVariable Long programmeYearId) {
        matchService.deleteMatchesByProgrammeYear(programmeYearId);
        return ResponseEntity.ok("All matches deleted and participants reset for ProgrammeYear ID: " + programmeYearId);
    }
}
