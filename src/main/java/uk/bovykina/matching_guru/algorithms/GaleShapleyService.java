package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.algorithms.helpers.MentorshipValidator;
import uk.bovykina.matching_guru.algorithms.helpers.MatchingCriteriaProvider;
import uk.bovykina.matching_guru.algorithms.interfaces.CompatibilityCalculator;
import uk.bovykina.matching_guru.algorithms.helpers.MatchSaver;
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
    private final CompatibilityCalculator compatibilityCalculator;
    private final MatchingCriteriaProvider criteriaProvider;
    private final MatchSaver matchSaver;
    private final MentorshipValidator mentorshipValidator;
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
                .toList();
    }

    private Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> runGaleShapley(
            List<ParticipantInProgrammeYear> mentors,
            List<ParticipantInProgrammeYear> mentees,
            Map<String, Integer> weights
    ) {
        Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches = new ConcurrentHashMap<>();
        Map<ParticipantInProgrammeYear, Queue<ParticipantInProgrammeYear>> mentorPreferences = new HashMap<>();

        for (ParticipantInProgrammeYear mentor : mentors) {
            List<ParticipantInProgrammeYear> rankedMentees = mentees.stream()
                    .filter(mentee -> mentorshipValidator.isCompatible(mentor, mentee))
                    .sorted(Comparator.comparingDouble(m -> -compatibilityCalculator.calculate(mentor, m, weights)))
                    .toList();
            if (!rankedMentees.isEmpty()) {
                mentorPreferences.put(mentor, new LinkedList<>(rankedMentees));
            }
        }

        Queue<ParticipantInProgrammeYear> freeMentors = new LinkedList<>(mentorPreferences.keySet());

        while (!freeMentors.isEmpty()) {
            ParticipantInProgrammeYear mentor = freeMentors.poll();
            Queue<ParticipantInProgrammeYear> preferences = mentorPreferences.get(mentor);

            while (preferences != null && !preferences.isEmpty()) {
                ParticipantInProgrammeYear mentee = preferences.poll();
                double score = compatibilityCalculator.calculate(mentor, mentee, weights);
                if (score == 0) continue;

                if (!isAlreadyMatched(matches, mentee)) {
                    matches.computeIfAbsent(mentor, k -> new ArrayList<>()).add(mentee);
                    break;
                } else {
                    ParticipantInProgrammeYear currentMentor = findCurrentMentor(matches, mentee);
                    double currentScore = compatibilityCalculator.calculate(currentMentor, mentee, weights);
                    if (score > currentScore) {
                        matches.get(currentMentor).remove(mentee);
                        matches.computeIfAbsent(mentor, k -> new ArrayList<>()).add(mentee);
                        freeMentors.add(currentMentor);
                        break;
                    }
                }
            }
        }

        return matches;
    }

    private boolean isAlreadyMatched(Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches, ParticipantInProgrammeYear mentee) {
        return matches.values().stream().anyMatch(list -> list.contains(mentee));
    }

    private ParticipantInProgrammeYear findCurrentMentor(Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches,
                                                         ParticipantInProgrammeYear mentee) {
        return matches.entrySet().stream()
                .filter(e -> e.getValue().contains(mentee))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
    }
}
