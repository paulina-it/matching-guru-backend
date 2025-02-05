package uk.bovykina.matching_guru.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.algorithms.GaleShapleyService;
import uk.bovykina.matching_guru.dto.match.MatchDto;
import uk.bovykina.matching_guru.dto.match.MatchResponseDto;
import uk.bovykina.matching_guru.service.MatchService;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {
    private final GaleShapleyService galeShapleyService;
    private final MatchService matchService;

    @PostMapping("/run")
    public void matchParticipants(@RequestParam Long programmeId, boolean isInitial) {
        galeShapleyService.matchParticipants(programmeId, isInitial);
    }

    @GetMapping("/{id}")
    public List<MatchResponseDto> getMatchesByProgrammeYearId(@PathVariable Long id) {
        return matchService.getMatchesByProgrammeYearId(id);
    }
}
