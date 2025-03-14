package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.algorithms.BraceService;
import uk.bovykina.matching_guru.algorithms.CollaborativeFilteringService;
import uk.bovykina.matching_guru.algorithms.GaleShapleyService;
import uk.bovykina.matching_guru.dto.match.MatchResponseDto;
import uk.bovykina.matching_guru.service.MatchService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Slf4j
public class MatchingController {

    private final GaleShapleyService galeShapleyService;
    private final BraceService braceService;
    private final CollaborativeFilteringService collaborativeFilteringService;
    private final MatchService matchService;

    @PostMapping("/{algorithm}/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void matchParticipants(
            @PathVariable String algorithm,
            @RequestParam Long programmeId,
            @RequestParam(required = false, defaultValue = "true") boolean isInitial
    ) {
        log.info("▶ Starting matching process with algorithm: {}", algorithm);

        switch (algorithm.toLowerCase()) {
            case "gale-shapley":
                galeShapleyService.matchParticipants(programmeId, isInitial);
                log.info("✔ Gale-Shapley algorithm completed for ProgrammeYear ID: {}", programmeId);
                break;
            case "brace":
                braceService.matchParticipantsWithBrace(programmeId);
                log.info("✔ BRACE algorithm completed for ProgrammeYear ID: {}", programmeId);
                break;
            case "collaborative-filtering":
                collaborativeFilteringService.collaborativeFilteringMatch(programmeId);
                log.info("✔ Collaborative Filtering algorithm completed for ProgrammeYear ID: {}", programmeId);
                break;
            default:
                throw new IllegalArgumentException("Invalid matching algorithm: " + algorithm);
        }
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
