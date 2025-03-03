package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.match.DetailedMatchResponseDto;
import uk.bovykina.matching_guru.dto.match.MatchResponseDto;
import uk.bovykina.matching_guru.dto.match.MatchStatusUpdateDto;
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

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponseDto> getMatchById(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.getMatchById(matchId));
    }

    @GetMapping("/detailed/{matchId}")
    public ResponseEntity<DetailedMatchResponseDto> getDetailedMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.getDetailedMatchById(matchId));
    }

    @GetMapping("/detailed/participant/{participantId}")
    public ResponseEntity<DetailedMatchResponseDto> getDetailedMatchByParticipantId(@PathVariable Long participantId) {
        return ResponseEntity.ok(matchService.getDetailedMatchByParticipantId(participantId));
    }

    @GetMapping("/programmeYear/{programmeYearId}")
    public ResponseEntity<Page<MatchResponseDto>> getMatchesByProgrammeYear(
            @PathVariable Long programmeYearId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Page<MatchResponseDto> matchesPage = matchService.getMatchesByProgrammeYearId(programmeYearId, PageRequest.of(page, size));
        return ResponseEntity.ok(matchesPage);
    }

    @GetMapping("/all")
    public ResponseEntity<List<MatchResponseDto>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @PatchMapping("/update-status")
    public ResponseEntity<String> updateMatchStatus(@RequestBody MatchStatusUpdateDto request) {
        List<Long> matchIds = request.getMatchIds();
        MatchStatus status = request.getStatus();

        matchService.updateMatchStatus(matchIds, status);

        return ResponseEntity.ok("Matches updated successfully");
    }

}
