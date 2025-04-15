package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.stats.OrganisationMatchStatsDto;
import uk.bovykina.matching_guru.dto.stats.ProgrammeMatchStatsDto;
import uk.bovykina.matching_guru.dto.stats.ProgrammeYearMatchStatsDto;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.ProgrammeYearRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final ProgrammeYearRepository programmeYearRepo;
    private final ParticipantRepository participantRepository;
    private final OrganisationRepository organisationRepository;

    public OrganisationMatchStatsDto getOrganisationStats(Long organisationId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with id: " + organisationId));

        OrganisationMatchStatsDto orgStats = new OrganisationMatchStatsDto();
        orgStats.setOrganisation(organisation.getName());

        List<ProgrammeYear> allYears = programmeYearRepo.findByProgramme_Organisation_Id(organisationId);
        Map<String, List<ProgrammeYear>> groupedByProgramme = allYears.stream()
                .collect(Collectors.groupingBy(p -> p.getProgramme().getName()));

        List<ProgrammeMatchStatsDto> programmeStatsList = new ArrayList<>();
        int totalParticipants = 0;
        int totalMatches = 0;

        for (Map.Entry<String, List<ProgrammeYear>> entry : groupedByProgramme.entrySet()) {
            String programmeName = entry.getKey();
            List<ProgrammeYear> years = entry.getValue();

            ProgrammeMatchStatsDto programmeStats = new ProgrammeMatchStatsDto();
            programmeStats.setProgrammeName(programmeName);

            int programmeTotal = 0;
            int programmeMatches = 0;

            List<ProgrammeYearMatchStatsDto> yearStats = new ArrayList<>();
            for (ProgrammeYear year : years) {
                int participants = participantRepository.countByProgrammeYearId(year.getId());
                int unmatched = participantRepository.countByProgrammeYearIdAndIsMatchedFalse(year.getId());
                int matches = participants - unmatched;

                programmeTotal += participants;
                programmeMatches += matches;

                ProgrammeYearMatchStatsDto yearDto = new ProgrammeYearMatchStatsDto();
                yearDto.setAcademicYear(year.getAcademicYear());
                yearDto.setParticipants(participants);
                yearDto.setMatches(matches);
                yearDto.setRate(participants == 0 ? 0 : (matches * 100.0 / participants));
                yearStats.add(yearDto);
            }

            programmeStats.setYears(yearStats);
            programmeStats.setTotalParticipants(programmeTotal);
            programmeStats.setTotalMatches(programmeMatches);
            programmeStats.setMatchRate(
                    programmeTotal == 0 ? 0 : (programmeMatches * 100.0 / programmeTotal)
            );

            totalParticipants += programmeTotal;
            totalMatches += programmeMatches;

            programmeStatsList.add(programmeStats);
        }

        orgStats.setTotalParticipants(totalParticipants);
        orgStats.setTotalMatches(totalMatches);
        orgStats.setMatchRatePercent(
                totalParticipants == 0 ? 0 : (totalMatches * 100.0 / totalParticipants)
        );
        orgStats.setProgrammes(programmeStatsList);

        return orgStats;
    }
}
