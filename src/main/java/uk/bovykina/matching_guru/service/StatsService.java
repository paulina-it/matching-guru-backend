package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.stats.demographics.*;
import uk.bovykina.matching_guru.dto.stats.engagement.OrganisationEngagementStatsDto;
import uk.bovykina.matching_guru.dto.stats.engagement.ProgrammeEngagementStatsDto;
import uk.bovykina.matching_guru.dto.stats.engagement.ProgrammeYearEngagementStatsDto;
import uk.bovykina.matching_guru.dto.stats.engagement.WeeklyEngagementPoint;
import uk.bovykina.matching_guru.dto.stats.match.OrganisationMatchStatsDto;
import uk.bovykina.matching_guru.dto.stats.match.ProgrammeMatchStatsDto;
import uk.bovykina.matching_guru.dto.stats.match.ProgrammeYearMatchStatsDto;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.repository.*;

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
        int totalAccepted = 0;
        int totalRejected = 0;
        int totalAcceptedByBoth = 0;
        int totalAcceptedByOne = 0;
        int totalPending = 0;

        for (Map.Entry<String, List<ProgrammeYear>> entry : groupedByProgramme.entrySet()) {
            String programmeName = entry.getKey();
            List<ProgrammeYear> years = entry.getValue();

            ProgrammeMatchStatsDto programmeStats = new ProgrammeMatchStatsDto();
            programmeStats.setProgrammeName(programmeName);

            int programmeTotal = 0;
            int programmeMatches = 0;
            int programmeAccepted = 0;
            int programmeRejected = 0;
            int programmeAcceptedByBoth = 0;
            int programmeAcceptedByOne = 0;
            int programmePending = 0;

            List<ProgrammeYearMatchStatsDto> yearStats = new ArrayList<>();

            for (ProgrammeYear year : years) {
                long yearId = year.getId();
                int participants = participantCounts.getOrDefault(yearId, 0);
                int matches = matchedCounts.getOrDefault(yearId, 0);

                programmeTotal += participants;
                programmeMatches += matches;

                List<Match> matchesForYear = matchRepository.findAllByProgrammeYearId(yearId);

                int acceptedByBoth = (int) matchesForYear.stream()
                        .filter(m -> m.getStatus() == MatchStatus.ACCEPTED_BY_BOTH)
                        .count();
                int acceptedByOne = (int) matchesForYear.stream()
                        .filter(m -> m.getStatus() == MatchStatus.ACCEPTED_BY_ONE_PARTY)
                        .count();
                int pending = (int) matchesForYear.stream()
                        .filter(m -> m.getStatus() == MatchStatus.PENDING)
                        .count();
                int rejected = (int) matchesForYear.stream()
                        .filter(m -> m.getStatus() == MatchStatus.REJECTED)
                        .count();

                ProgrammeYearMatchStatsDto yearDto = new ProgrammeYearMatchStatsDto();
                yearDto.setAcademicYear(year.getAcademicYear());
                yearDto.setParticipants(participants);
                yearDto.setMatches(matches);
                yearDto.setRate(participants == 0 ? 0 : (matches * 100.0 / participants));
                yearDto.setAccepted(acceptedByBoth + acceptedByOne);
                yearDto.setAcceptedByBoth(acceptedByBoth);
                yearDto.setAcceptedByOne(acceptedByOne);
                yearDto.setPending(pending);
                yearDto.setRejected(rejected);
                yearDto.setAcceptRate(matches == 0 ? 0 : ((acceptedByBoth + acceptedByOne) * 100.0 / matches));
                yearDto.setRejectRate(matches == 0 ? 0 : (rejected * 100.0 / matches));

                yearStats.add(yearDto);

                programmeAccepted += (acceptedByBoth + acceptedByOne);
                programmeAcceptedByBoth += acceptedByBoth;
                programmeAcceptedByOne += acceptedByOne;
                programmePending += pending;
                programmeRejected += rejected;
            }

            programmeStats.setYears(yearStats);
            programmeStats.setTotalParticipants(programmeTotal);
            programmeStats.setTotalMatches(programmeMatches);
            programmeStats.setAccepted(programmeAccepted);
            programmeStats.setRejected(programmeRejected);
            programmeStats.setAcceptedByBoth(programmeAcceptedByBoth);
            programmeStats.setAcceptedByOne(programmeAcceptedByOne);
            programmeStats.setPending(programmePending);
            programmeStats.setMatchRate(programmeTotal == 0 ? 0 : (programmeMatches * 100.0 / programmeTotal));
            programmeStats.setAcceptRate(programmeMatches == 0 ? 0 : (programmeAccepted * 100.0 / programmeMatches));
            programmeStats.setRejectRate(programmeMatches == 0 ? 0 : (programmeRejected * 100.0 / programmeMatches));

            totalParticipants += programmeTotal;
            totalMatches += programmeMatches;
            totalAccepted += programmeAccepted;
            totalRejected += programmeRejected;
            totalAcceptedByBoth += programmeAcceptedByBoth;
            totalAcceptedByOne += programmeAcceptedByOne;
            totalPending += programmePending;

            programmeStatsList.add(programmeStats);
        }

        orgStats.setTotalParticipants(totalParticipants);
        orgStats.setTotalMatches(totalMatches);
        orgStats.setMatchRatePercent(totalParticipants == 0 ? 0 : (totalMatches * 100.0 / totalParticipants));
        orgStats.setOverallAcceptRate(totalMatches == 0 ? 0 : (totalAccepted * 100.0 / totalMatches));
        orgStats.setOverallRejectRate(totalMatches == 0 ? 0 : (totalRejected * 100.0 / totalMatches));
        orgStats.setAcceptedByBoth(totalAcceptedByBoth);
        orgStats.setAcceptedByOne(totalAcceptedByOne);
        orgStats.setPending(totalPending);
        orgStats.setRejected(totalRejected);
        orgStats.setProgrammes(programmeStatsList);

        return orgStats;
    }

    @Transactional(readOnly = true)
    public OrganisationEngagementStatsDto getOrganisationEngagementStats(Long organisationId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with id: " + organisationId));

        List<ProgrammeYear> allYears = programmeYearRepo.findByProgramme_Organisation_Id(organisationId);
        List<Long> yearIds = allYears.stream().map(ProgrammeYear::getId).toList();

        Map<Long, Long> participantCounts = participantRepository.countParticipantsByProgrammeYear(yearIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        Map<Long, Long> feedbackCounts = endSurveyResponseRepository.countSurveysByProgrammeYear(yearIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        Map<Long, Map<String, Long>> communicationBreakdown = new HashMap<>();
        for (Object[] row : communicationLogRepository.countLogsByYearAndType(yearIds)) {
            Long yearId = (Long) row[0];
            String type = row[1].toString();
            Long count = (Long) row[2];
            communicationBreakdown.computeIfAbsent(yearId, k -> new HashMap<>()).put(type, count);
        }

        Map<Long, Map<MatchStatus, Long>> matchStatusCounts = new HashMap<>();
        for (Object[] row : matchRepository.countMatchesByStatus(yearIds)) {
            Long yearId = (Long) row[0];
            MatchStatus status = (MatchStatus) row[1];
            Long count = (Long) row[2];
            matchStatusCounts.computeIfAbsent(yearId, k -> new HashMap<>()).put(status, count);
        }

        List<Programme> programmes = programmeRepository.findByOrganisationId(organisationId);
        List<ProgrammeEngagementStatsDto> programmeStatsList = new ArrayList<>();

        for (Programme programme : programmes) {
            ProgrammeEngagementStatsDto programmeStats = new ProgrammeEngagementStatsDto();
            programmeStats.setProgrammeName(programme.getName());

            List<ProgrammeYear> years = allYears.stream()
                    .filter(y -> y.getProgramme().getId().equals(programme.getId()))
                    .toList();

            List<ProgrammeYearEngagementStatsDto> yearStatsList = new ArrayList<>();

            for (ProgrammeYear year : years) {
                Long yearId = year.getId();
                ProgrammeYearEngagementStatsDto yearStats = new ProgrammeYearEngagementStatsDto();
                yearStats.setAcademicYear(year.getAcademicYear());
                yearStats.setParticipants(participantCounts.getOrDefault(yearId, 0L).intValue());

                int feedback = feedbackCounts.getOrDefault(yearId, 0L).intValue();
                yearStats.setFeedbackSubmitted(feedback);
                yearStats.setFeedbackCompletionRate(feedback == 0 ? 0.0 : (feedback * 100.0 / participantCounts.getOrDefault(yearId, 0L).intValue()));

                Map<MatchStatus, Long> statusMap = matchStatusCounts.getOrDefault(yearId, Map.of());
                yearStats.setInactivePairs(statusMap.getOrDefault(MatchStatus.PENDING, 0L).intValue());

                long totalMatches = statusMap.values().stream().mapToLong(Long::longValue).sum();
                long acceptedByBoth = statusMap.getOrDefault(MatchStatus.ACCEPTED_BY_BOTH, 0L);
                long acceptedByOne = statusMap.getOrDefault(MatchStatus.ACCEPTED_BY_ONE_PARTY, 0L);
                yearStats.setAvgInteractionsPerMatch(totalMatches == 0 ? 0.0 : communicationBreakdown.getOrDefault(yearId, Map.of()).values().stream().mapToLong(Long::longValue).sum() / (double) totalMatches);

                Map<String, Integer> breakdown = new HashMap<>();
                communicationBreakdown.getOrDefault(yearId, Map.of()).forEach((k, v) -> breakdown.put(k, v.intValue()));
                yearStats.setCommunicationBreakdown(breakdown);

                yearStats.setWeeklyEngagement(getWeeklyEngagementStats(yearId));

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

    @Transactional(readOnly = true)
    public OrganisationDemographicStatsDto getOrganisationDemographics(Long orgId) {
        Organisation org = organisationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Org not found"));

        List<ProgrammeYear> years = programmeYearRepo.findByProgramme_Organisation_Id(orgId);
        List<Long> yearIds = years.stream().map(ProgrammeYear::getId).toList();

        Map<Long, Map<String, Long>> courseByYear = aggregateByYear(participantRepository.countByCourse(yearIds));
        Map<Long, Map<String, Long>> stageByYear = aggregateByYear(participantRepository.countByStage(yearIds));

        List<Object[]> groupRows = participantRepository.countCourseGroupsByProgrammeYears(yearIds);
        Map<Long, Map<String, Long>> groupByYear = aggregateByYear(groupRows);

        Map<String, Long> groupTotals = new HashMap<>();
        for (Object[] row : groupRows) {
            String groupName = (String) row[1];
            Long count = (Long) row[2];
            groupTotals.merge(groupName, count, Long::sum);
        }

        long orgTotal = 0;
        Map<String, Long> orgCourse = new HashMap<>();
        Map<String, Long> orgStage = new HashMap<>();
        Map<String, Long> orgGroups = new HashMap<>();

        Map<Long, List<ProgrammeYear>> byProgramme = years.stream()
                .collect(Collectors.groupingBy(py -> py.getProgramme().getId()));

        List<ProgrammeDemoDto> programmeDtos = new ArrayList<>();

        for (var progEntry : byProgramme.entrySet()) {
            Programme prog = progEntry.getValue().get(0).getProgramme();
            List<ProgrammeYearDemoDto> yearDtos = new ArrayList<>();

            for (ProgrammeYear y : progEntry.getValue()) {
                long yId = y.getId();

                Map<String, Long> cMap = courseByYear.getOrDefault(yId, Map.of());
                Map<String, Long> sMap = stageByYear.getOrDefault(yId, Map.of());
                Map<String, Long> gMap = groupByYear.getOrDefault(yId, Map.of());

                long total = cMap.values().stream().mapToLong(Long::longValue).sum();
                orgTotal += total;

                cMap.forEach((k, v) -> orgCourse.merge(k, v, Long::sum));
                sMap.forEach((k, v) -> orgStage.merge(k, v, Long::sum));
                gMap.forEach((k, v) -> orgGroups.merge(k, v, Long::sum));

                yearDtos.add(new ProgrammeYearDemoDto(
                        y.getAcademicYear(),
                        toCourseList(cMap),
                        toStageList(sMap),
                        total,
                        toGroupList(gMap)
                ));
            }

            programmeDtos.add(new ProgrammeDemoDto(prog.getName(), yearDtos));
        }

        return new OrganisationDemographicStatsDto(
                org.getName(),
                orgTotal,
                toCourseList(orgCourse),
                toStageList(orgStage),
                toGroupList(groupTotals),
                programmeDtos
        );
    }

    private Map<Long, Map<String, Long>> aggregateByYear(List<Object[]> rows) {
        Map<Long, Map<String, Long>> result = new HashMap<>();
        for (Object[] r : rows) {
            Long yearId = (Long) r[0];
            String name = (r[1] instanceof Enum<?> e) ? e.name() : r[1].toString();
            Long cnt = (Long) r[2];
            result.computeIfAbsent(yearId, __ -> new HashMap<>()).put(name, cnt);
        }
        return result;
    }

    private Map<String, Long> aggregateByName(List<Object[]> rows) {
        Map<String, Long> result = new HashMap<>();
        for (Object[] r : rows) {
            String name = (String) r[0];
            Long cnt = (Long) r[1];
            result.put(name, cnt);
        }
        return result;
    }

    private List<CourseBreakdownDto> toCourseList(Map<String, Long> map) {
        return map.entrySet().stream()
                .map(e -> new CourseBreakdownDto(e.getKey(), e.getValue()))
                .toList();
    }

    private List<StageBreakdownDto> toStageList(Map<String, Long> map) {
        return map.entrySet().stream()
                .map(e -> new StageBreakdownDto(e.getKey(), e.getValue()))
                .toList();
    }

    private List<CourseGroupBreakdownDto> toGroupList(Map<String, Long> map) {
        return map.entrySet().stream()
                .map(e -> new CourseGroupBreakdownDto(e.getKey(), e.getValue().intValue()))
                .toList();
    }

    private List<WeeklyEngagementPoint> getWeeklyEngagementStats(Long yearId) {
        List<Object[]> weeklyData = communicationLogRepository.findWeeklyEngagementByYear(yearId);
        return weeklyData.stream()
                .map(row -> new WeeklyEngagementPoint((String) row[0], ((Long) row[1]).intValue()))
                .collect(Collectors.toList());
    }

}
