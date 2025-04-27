package uk.bovykina.matching_guru.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.stats.demographics.OrganisationDemographicStatsDto;
import uk.bovykina.matching_guru.dto.stats.engagement.OrganisationEngagementStatsDto;
import uk.bovykina.matching_guru.dto.stats.match.OrganisationMatchStatsDto;
import uk.bovykina.matching_guru.service.StatsService;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;

    /**
     * Returns match statistics for the specified organisation.
     */
    @GetMapping("/match-rates/organisation/{organisationId}")
    public OrganisationMatchStatsDto getOrganisationMatchStats(@PathVariable Long organisationId) {
        log.info("Fetching match stats for organisation ID: {}", organisationId);

        OrganisationMatchStatsDto stats = statsService.getOrganisationStats(organisationId);

        log.info("Stats retrieved for organisation ID {}: {} participants, {} matches, match rate: {}%",
                organisationId,
                stats.getTotalParticipants(),
                stats.getTotalMatches(),
                String.format("%.2f", stats.getMatchRatePercent())
        );

        return stats;
    }

    /**
     * Returns engagement statistics for the specified organisation.
     */
    @GetMapping("/engagement/organisation/{organisationId}")
    public OrganisationEngagementStatsDto getEngagementStats(@PathVariable Long organisationId) {
        log.info("Fetching engagement stats for organisation ID: {}", organisationId);
        OrganisationEngagementStatsDto stats = statsService.getOrganisationEngagementStats(organisationId);

        log.info("Retrieved engagement stats for org '{}', with {} programmes",
                stats.getOrganisation(), stats.getProgrammes().size());

        return stats;
    }

    /**
     * Returns demographic statistics for the specified organisation.
     */
    @GetMapping("/demographics/organisation/{organisationId}")
    public ResponseEntity<OrganisationDemographicStatsDto> demographics(
            @Parameter(example = "10") @PathVariable Long organisationId) {

        log.info("📊 Fetching demographic stats for organisation ID: {}", organisationId);
        return ResponseEntity.ok(
                statsService.getOrganisationDemographics(organisationId));
    }
}
