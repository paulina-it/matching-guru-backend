package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.algorithms.helpers.MatchSaver;
import uk.bovykina.matching_guru.algorithms.helpers.MentorshipValidator;
import uk.bovykina.matching_guru.algorithms.interfaces.CompatibilityCalculator;
import uk.bovykina.matching_guru.algorithms.helpers.MatchingCriteriaProvider;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.MatchRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.service.ProgrammeYearService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaleShapleyService {

    private final ParticipantRepository participantRepository;
    private final CompatibilityService compatibilityService;
    private final MentorshipValidator mentorshipValidator;
    private final MatchingCriteriaProvider criteriaProvider;
    private final MatchSaver matchSaver;
    private final ProgrammeYearService programmeYearService;
    private final MatchRepository matchRepository;

    @Transactional
    public void matchParticipants(Long programmeYearId, boolean isInitialMatching) {
        log.info("\u25B6 Starting Gale-Shapley for ProgrammeYear ID: {} | Initial: {}", programmeYearId, isInitialMatching);

        ProgrammeYear programmeYear = programmeYearService.getById(programmeYearId);

        List<ParticipantInProgrammeYear> mentors = loadParticipants(programmeYearId, ParticipantRole.MENTOR, isInitialMatching).stream().sorted(Comparator.comparing(p -> Boolean.TRUE.equals(p.getWasMatchedLastYear()))).collect(Collectors.toList());

        List<ParticipantInProgrammeYear> mentees = loadParticipants(programmeYearId, ParticipantRole.MENTEE, isInitialMatching).stream().sorted(Comparator.comparing(p -> Boolean.TRUE.equals(p.getWasMatchedLastYear()))).collect(Collectors.toList());

        if (mentors.isEmpty() || mentees.isEmpty()) {
            log.warn("\u26A0 Not enough participants to match");
            return;
        }

        Map<String, Integer> weights = criteriaProvider.loadWeights(programmeYearId);
        Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches = runGaleShapley(mentors, mentees, weights);

        matchSaver.saveMatches(programmeYearId, programmeYear, matches, weights);
        log.info("\u2714 Matching complete for ProgrammeYear ID: {}", programmeYearId);
    }

    private List<ParticipantInProgrammeYear> loadParticipants(Long programmeYearId, ParticipantRole role, boolean isInitialMatching) {
        return participantRepository.findByProgrammeYearId(programmeYearId).stream().filter(p -> p.getRole() == role).filter(p -> isInitialMatching || !Boolean.TRUE.equals(p.getIsMatched())).sorted(Comparator.comparing(p -> Boolean.TRUE.equals(p.getWasMatchedLastYear()))).toList();
    }

    private Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> runGaleShapley(List<ParticipantInProgrammeYear> mentors, List<ParticipantInProgrammeYear> mentees, Map<String, Integer> weights) {
        Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches = new ConcurrentHashMap<>();
        Map<ParticipantInProgrammeYear, Queue<ParticipantInProgrammeYear>> mentorPreferences = new HashMap<>();
        Map<ParticipantInProgrammeYear, Integer> mentorLoad = new HashMap<>();

        for (ParticipantInProgrammeYear mentor : mentors) {
            int allowed = mentor.getMenteesNumber() != null ? mentor.getMenteesNumber() : 1;
            mentorLoad.put(mentor, 0);

            List<ParticipantInProgrammeYear> compatibleMentees = mentees.stream().filter(mentee -> {
                if (mentor.getUser().getId().equals(mentee.getUser().getId())) return false;
                if (!mentorshipValidator.isCompatibleWithHistoryCheck(mentor, mentee, true, true)) return false;
                return compatibilityService.calculate(mentor, mentee, weights) > 0;
            }).sorted(Comparator.comparingDouble(mentee -> -compatibilityService.calculate(mentor, mentee, weights))).limit(5).collect(Collectors.toList());

            if (!compatibleMentees.isEmpty()) {
                mentorPreferences.put(mentor, new LinkedList<>(compatibleMentees));
            }
        }

        Queue<ParticipantInProgrammeYear> freeMentors = new LinkedList<>(mentorPreferences.keySet());
        log.info("\uD83D\uDD04 Starting Gale-Shapley algorithm with {} active mentors", freeMentors.size());

        while (!freeMentors.isEmpty()) {
            ParticipantInProgrammeYear mentor = freeMentors.poll();
            Queue<ParticipantInProgrammeYear> preferences = mentorPreferences.get(mentor);

            int allowed = mentor.getMenteesNumber() != null ? mentor.getMenteesNumber() : 1;
            int currentLoad = mentorLoad.getOrDefault(mentor, 0);

            if (preferences == null || preferences.isEmpty() || currentLoad >= allowed) {
                log.debug("\u274C Mentor {} has no more capacity or preferences left", mentor.getId());
                continue;
            }

            while (!preferences.isEmpty() && mentorLoad.get(mentor) < allowed) {
                ParticipantInProgrammeYear mentee = preferences.poll();
                double compatibilityScore = compatibilityService.calculate(mentor, mentee, weights);

                boolean menteeMatched = matches.values().stream().flatMap(List::stream).anyMatch(m -> m.equals(mentee));

                if (compatibilityScore == 0 || menteeMatched) continue;

                matches.computeIfAbsent(mentor, k -> new ArrayList<>()).add(mentee);
                mentorLoad.put(mentor, mentorLoad.get(mentor) + 1);

                log.info("\uD83D\uDD17 Created match: Mentor {} → Mentee {} (score = {})", mentor.getId(), mentee.getId(), compatibilityScore);

                if (mentorLoad.get(mentor) < allowed && !preferences.isEmpty()) {
                    freeMentors.add(mentor);
                }

                break;
            }
        }

        List<ParticipantInProgrammeYear> unmatchedMentees = mentees.stream().filter(m -> matches.values().stream().flatMap(List::stream).noneMatch(mm -> mm.equals(m))).toList();

        for (ParticipantInProgrammeYear mentee : unmatchedMentees) {
            Optional<ParticipantInProgrammeYear> bestFallbackMentor = mentors.stream().filter(mentor -> !mentor.getUser().getId().equals(mentee.getUser().getId())).filter(mentor -> mentorLoad.getOrDefault(mentor, 0) < (mentor.getMenteesNumber() != null ? mentor.getMenteesNumber() : 1)).filter(mentor -> mentorshipValidator.isCompatibleWithHistoryCheck(mentor, mentee, false, true))

                    .filter(mentor -> mentor.getCourseGroup() != null && mentee.getCourseGroup() != null && mentor.getCourseGroup().getId().equals(mentee.getCourseGroup().getId())).sorted(Comparator.comparingDouble((ParticipantInProgrammeYear mentor) -> -compatibilityService.calculate(mentor, mentee, weights))).findFirst();

            bestFallbackMentor.ifPresent(mentor -> {
                matches.computeIfAbsent(mentor, k -> new ArrayList<>()).add(mentee);
                mentorLoad.put(mentor, mentorLoad.getOrDefault(mentor, 0) + 1);
                double score = compatibilityService.calculate(mentor, mentee, weights);
                log.info("\uD83D\uDFE1 Fallback match: Mentee {} → Mentor {} (score = {})", mentee.getId(), mentor.getId(), score);
            });
        }

        mentees.stream().filter(m -> matches.values().stream().flatMap(List::stream).noneMatch(mm -> mm.equals(m))).forEach(m -> log.warn("\u274C Unmatched mentee: {}", m.getId()));

        return matches;
    }
}
