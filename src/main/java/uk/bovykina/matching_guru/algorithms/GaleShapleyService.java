// Refactored Gale-Shapley with modular helpers and high match rate preserved
package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.algorithms.helpers.MatchSaver;
import uk.bovykina.matching_guru.algorithms.helpers.MentorshipValidator;
import uk.bovykina.matching_guru.algorithms.interfaces.CompatibilityCalculator;
import uk.bovykina.matching_guru.algorithms.helpers.MatchingCriteriaProvider;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
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

    @Transactional
    public void matchParticipants(Long programmeYearId, boolean isInitialMatching) {
        log.info("▶ Starting Gale-Shapley for ProgrammeYear ID: {} | Initial: {}", programmeYearId, isInitialMatching);

        ProgrammeYear programmeYear = programmeYearService.getById(programmeYearId);

        List<ParticipantInProgrammeYear> mentors = loadParticipants(programmeYearId, ParticipantRole.MENTOR, isInitialMatching);
        List<ParticipantInProgrammeYear> mentees = loadParticipants(programmeYearId, ParticipantRole.MENTEE, isInitialMatching);

        if (mentors.isEmpty() || mentees.isEmpty()) {
            log.warn("⚠ Not enough participants to match");
            return;
        }

        Map<String, Integer> weights = criteriaProvider.loadWeights(programmeYearId);
        Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches = runGaleShapley(mentors, mentees, weights);

        matchSaver.saveMatches(programmeYearId, programmeYear, matches, weights);
        log.info("✔ Matching complete for ProgrammeYear ID: {}", programmeYearId);
    }

    private List<ParticipantInProgrammeYear> loadParticipants(Long programmeYearId, ParticipantRole role, boolean isInitialMatching) {
        return participantRepository.findByProgrammeYearIdAndRole(programmeYearId, role).stream()
                .filter(p -> isInitialMatching || !Boolean.TRUE.equals(p.getIsMatched()))
                .collect(Collectors.toList());
    }

    private Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> runGaleShapley(
            List<ParticipantInProgrammeYear> mentors,
            List<ParticipantInProgrammeYear> mentees,
            Map<String, Integer> weights
    ) {
        Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches = new ConcurrentHashMap<>();
        Map<ParticipantInProgrammeYear, Queue<ParticipantInProgrammeYear>> mentorPreferences = new HashMap<>();
        Map<ParticipantInProgrammeYear, Integer> mentorLoad = new HashMap<>();

        for (ParticipantInProgrammeYear mentor : mentors) {
            int allowed = mentor.getMenteesNumber() != null ? mentor.getMenteesNumber() : 1;
            mentorLoad.put(mentor, 0);

            List<ParticipantInProgrammeYear> compatibleMentees = mentees.stream().filter(mentee -> {
                        if (!mentorshipValidator.isCompatible(mentor, mentee, true)) return false;
                        return compatibilityService.calculate(mentor, mentee, weights) > 0;
                    })
                    .sorted(Comparator.comparingDouble(mentee -> -compatibilityService.calculate(mentor, mentee, weights)))
                    .limit(5)
                    .collect(Collectors.toList());

            Collections.shuffle(compatibleMentees);
            if (!compatibleMentees.isEmpty()) {
                mentorPreferences.put(mentor, new LinkedList<>(compatibleMentees));
            }
        }

        Queue<ParticipantInProgrammeYear> freeMentors = new LinkedList<>(mentorPreferences.keySet());
        log.info("🔄 Starting Gale-Shapley algorithm with {} active mentors", freeMentors.size());

        while (!freeMentors.isEmpty()) {
            ParticipantInProgrammeYear mentor = freeMentors.poll();
            Queue<ParticipantInProgrammeYear> preferences = mentorPreferences.get(mentor);

            int allowed = mentor.getMenteesNumber() != null ? mentor.getMenteesNumber() : 1;
            int currentLoad = mentorLoad.getOrDefault(mentor, 0);

            if (preferences == null || preferences.isEmpty() || currentLoad >= allowed) {
                log.debug("🚫 Mentor {} has no more capacity or preferences left", mentor.getId());
                continue;
            }

            while (!preferences.isEmpty() && mentorLoad.get(mentor) < allowed) {
                ParticipantInProgrammeYear mentee = preferences.poll();
                double compatibilityScore = compatibilityService.calculate(mentor, mentee, weights);

                boolean menteeMatched = matches.values().stream()
                        .flatMap(List::stream)
                        .anyMatch(m -> m.equals(mentee));

                if (compatibilityScore == 0 || menteeMatched) continue;

                matches.computeIfAbsent(mentor, k -> new ArrayList<>()).add(mentee);
                mentorLoad.put(mentor, mentorLoad.get(mentor) + 1);

                log.info("🔗 Created match: Mentor {} → Mentee {} (score = {})", mentor.getId(), mentee.getId(), compatibilityScore);

                if (mentorLoad.get(mentor) < allowed && !preferences.isEmpty()) {
                    freeMentors.add(mentor);
                }

                break;
            }
        }

        // Fallback Phase: Unmatched Mentees
        List<ParticipantInProgrammeYear> unmatchedMentees = mentees.stream()
                .filter(m -> matches.values().stream().flatMap(List::stream).noneMatch(mm -> mm.equals(m)))
                .toList();

        for (ParticipantInProgrammeYear mentee : unmatchedMentees) {
            mentors.stream()
                    .filter(mentor -> mentorLoad.getOrDefault(mentor, 0) < (mentor.getMenteesNumber() != null ? mentor.getMenteesNumber() : 1))
                    .filter(mentor -> mentorshipValidator.isCompatible(mentor, mentee, true)) // ✅ enforce course group here
                    .sorted(Comparator.comparingDouble(m -> -compatibilityService.calculate(m, mentee, weights)))
                    .limit(3)
                    .forEach(mentor -> {
                        matches.computeIfAbsent(mentor, k -> new ArrayList<>()).add(mentee);
                        mentorLoad.put(mentor, mentorLoad.getOrDefault(mentor, 0) + 1);
                        double score = compatibilityService.calculate(mentor, mentee, weights);
                        log.info("🟡 Fallback match (reverse): Mentee {} → Mentor {} (score = {})", mentee.getId(), mentor.getId(), score);
                    });
        }


        // Logging unmatched
        mentees.stream()
                .filter(m -> matches.values().stream().flatMap(List::stream).noneMatch(mm -> mm.equals(m)))
                .forEach(m -> log.warn("❌ Unmatched mentee: {}", m.getId()));

        return matches;
    }
}