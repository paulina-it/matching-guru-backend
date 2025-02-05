package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.match.DetailedMatchResponseDto;
import uk.bovykina.matching_guru.dto.match.MatchResponseDto;
import uk.bovykina.matching_guru.service.MatchService;

import java.util.List;

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

    @GetMapping("/programmeYear/{programmeYearId}")
    public ResponseEntity<List<MatchResponseDto>> getMatchesByProgrammeYear(@PathVariable Long programmeYearId) {
        return ResponseEntity.ok(matchService.getMatchesByProgrammeYearId(programmeYearId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<MatchResponseDto>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }
}
