package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.stats.*;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.*;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final MatchRepository matchRepository;
    private final CommunicationLogRepository communicationLogRepository;
    private final EndSurveyResponseRepository endSurveyResponseRepository;
    private final ProgrammeRepository programmeRepository;

    public OrganisationMatchStatsDto getOrganisationStats(Long organisationId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with id: " + organisationId));

        OrganisationMatchStatsDto orgStats = new OrganisationMatchStatsDto();
        orgStats.setOrganisation(organisation.getName());

        List<ProgrammeYear> allYears = programmeYearRepo.findByProgramme_Organisation_Id(organisationId);
        List<Long> yearIds = allYears.stream().map(ProgrammeYear::getId).toList();

        Map<Long, Integer> participantCounts = participantRepository.countParticipantsByProgrammeYears(yearIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        Map<Long, Integer> matchedCounts = participantRepository.countMatchedByProgrammeYears(yearIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

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
                long yearId = year.getId();
                int participants = participantCounts.getOrDefault(yearId, 0);
                int matches = matchedCounts.getOrDefault(yearId, 0);

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


    public OrganisationEngagementStatsDto getOrganisationEngagementStats(Long organisationId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with id: " + organisationId));

        List<Programme> programmes = programmeRepository.findByOrganisationId(organisationId);
        List<ProgrammeEngagementStatsDto> programmeStatsList = new ArrayList<>();

        for (Programme programme : programmes) {
            ProgrammeEngagementStatsDto programmeStats = new ProgrammeEngagementStatsDto();
            programmeStats.setProgrammeName(programme.getName());

            List<ProgrammeYear> years = programmeYearRepo.findByProgrammeId(programme.getId());
            List<ProgrammeYearEngagementStatsDto> yearStatsList = new ArrayList<>();

            for (ProgrammeYear year : years) {
                Long yearId = year.getId();

                List<ParticipantInProgrammeYear> participants = participantRepository.findByProgrammeYearId(yearId);
                List<Match> matches = matchRepository.findAllByProgrammeYearId(yearId);
                List<CommunicationLog> logs = communicationLogRepository.findByMatch_ProgrammeYearId(yearId);
                List<EndSurveyResponse> surveys = endSurveyResponseRepository.findByProgrammeYearId(yearId);

                ProgrammeYearEngagementStatsDto yearStats = new ProgrammeYearEngagementStatsDto();
                yearStats.setAcademicYear(year.getAcademicYear());
                yearStats.setParticipants(participants.size());

                // Role breakdown
                int mentors = 0, mentees = 0;
                for (ParticipantInProgrammeYear p : participants) {
                    switch (p.getRole()) {
                        case MENTOR -> mentors++;
                        case MENTEE -> mentees++;
                    }
                }
                yearStats.setMentors(mentors);
                yearStats.setMentees(mentees);

                // Inactive matches: no communication in the last 14 days
                int inactivePairs = (int) matches.stream()
                        .filter(m -> logs.stream()
                                .noneMatch(log ->
                                        log.getMatch().getId().equals(m.getId()) &&
                                                log.getTimestamp().isAfter(LocalDateTime.now().minusDays(14)))
                        ).count();
                yearStats.setInactivePairs(inactivePairs);

                // Feedback submitted
                yearStats.setFeedbackSubmitted(surveys.size());

                // Average interactions per match
                double avgInteractions = matches.isEmpty() ? 0.0 : logs.size() / (double) matches.size();
                yearStats.setAvgInteractionsPerMatch(avgInteractions);

                // Communication breakdown
                Map<String, Integer> breakdown = new HashMap<>();
                for (CommunicationLog log : logs) {
                    String type = log.getType().name();
                    breakdown.put(type, breakdown.getOrDefault(type, 0) + 1);
                }
                yearStats.setCommunicationBreakdown(breakdown);

                // Weekly engagement counts
                Map<String, Long> weeklyCounts = logs.stream()
                        .collect(Collectors.groupingBy(
                                log -> log.getTimestamp().getYear() + "-W" +
                                        log.getTimestamp().get(WeekFields.ISO.weekOfWeekBasedYear()),
                                Collectors.counting()
                        ));

                List<WeeklyEngagementPoint> weekPoints = weeklyCounts.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(e -> {
                            WeeklyEngagementPoint point = new WeeklyEngagementPoint();
                            point.setWeek(e.getKey());
                            point.setInteractions(e.getValue().intValue());
                            return point;
                        }).toList();

                yearStats.setWeeklyEngagement(weekPoints);
                yearStatsList.add(yearStats);
            }

            programmeStats.setYears(yearStatsList);
            programmeStatsList.add(programmeStats);
        }

        OrganisationEngagementStatsDto result = new OrganisationEngagementStatsDto();
        result.setOrganisation(organisation.getName());
        result.setProgrammes(programmeStatsList);

        return result;
    }


}
