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

        log.debug("Loaded {} mentors and {} mentees for ProgrammeYear ID: {}", mentors.size(), mentees.size(), programmeYearId);

        if (mentors.isEmpty() || mentees.isEmpty()) {
            log.warn("⚠ Not enough participants to match for ProgrammeYear ID: {}", programmeYearId);
            return;
        }

        Map<String, Integer> weights = criteriaProvider.loadWeights(programmeYearId);
        log.debug("Loaded matching criteria weights: {}", weights);

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
        long startTime = System.nanoTime();
        Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches = new ConcurrentHashMap<>();
        Map<ParticipantInProgrammeYear, Queue<ParticipantInProgrammeYear>> mentorPreferences = new HashMap<>();

        for (ParticipantInProgrammeYear mentor : mentors) {
            List<ParticipantInProgrammeYear> rankedMentees = mentees.stream()
                    .filter(mentee -> mentorshipValidator.isCompatible(mentor, mentee))
                    .sorted(Comparator.comparingDouble(m -> -compatibilityCalculator.calculate(mentor, m, weights)))
                    .collect(Collectors.toList());

            if (!rankedMentees.isEmpty()) {
                mentorPreferences.put(mentor, new LinkedList<>(rankedMentees));
                log.debug("Mentor {} has {} preferred mentees.", mentor.getId(), rankedMentees.size());
            } else {
                log.debug("Mentor {} has no compatible mentees.", mentor.getId());
            }
        }

        Queue<ParticipantInProgrammeYear> freeMentors = new LinkedList<>(mentorPreferences.keySet());

        log.debug("Starting the Gale-Shapley algorithm with {} free mentors.", freeMentors.size());

        while (!freeMentors.isEmpty()) {
            ParticipantInProgrammeYear mentor = freeMentors.poll();
            log.debug("Processing mentor {}.", mentor.getId());

            Queue<ParticipantInProgrammeYear> preferences = mentorPreferences.get(mentor);

            if (matches.getOrDefault(mentor, Collections.emptyList()).size() >= mentor.getMenteesNumber()) {
                continue;
            }

            while (preferences != null && !preferences.isEmpty()) {
                ParticipantInProgrammeYear mentee = preferences.poll();
                double score = compatibilityCalculator.calculate(mentor, mentee, weights);
                log.debug("Evaluating match for Mentor {} and Mentee {}: score {}", mentor.getId(), mentee.getId(), score);

                if (score == 0) {
                    log.debug("Mentor {} and Mentee {} are not compatible (score 0).", mentor.getId(), mentee.getId());
                    continue;
                }

                if (!isAlreadyMatched(matches, mentee)) {
                    matches.computeIfAbsent(mentor, k -> new ArrayList<>()).add(mentee);
                    log.debug("Match found: Mentor {} -> Mentee {}", mentor.getId(), mentee.getId());
                    break;
                } else {
                    ParticipantInProgrammeYear currentMentor = findCurrentMentor(matches, mentee);
                    double currentScore = compatibilityCalculator.calculate(currentMentor, mentee, weights);

                    if (score > currentScore) {
                        log.debug("Mentor {} has a better score than current mentor {}. Replacing the match.", mentor.getId(), currentMentor.getId());
                        matches.get(currentMentor).remove(mentee);
                        matches.computeIfAbsent(mentor, k -> new ArrayList<>()).add(mentee);
                        freeMentors.add(currentMentor);
                        break;
                    } else {
                        log.debug("Mentor {} has a worse score than current mentor {}. Keeping current match.", mentor.getId(), currentMentor.getId());
                    }
                }
            }

            if (matches.getOrDefault(mentor, Collections.emptyList()).size() < mentor.getMenteesNumber()) {
                if (!preferences.isEmpty()) {
                    log.debug("Mentor {} needs more mentees. Re-adding to the queue.", mentor.getId());
                    freeMentors.add(mentor);
                } else {
                    log.debug("Mentor {} has no compatible mentees left. Not re-adding to the queue.", mentor.getId());
                }
            }
        }

        long endTime = System.nanoTime();
        long durationInMillis = (endTime - startTime) / 1_000_000;
        log.info("Execution time of Gale-Shapley algorithm: {} ms", durationInMillis);

        log.debug("Gale-Shapley algorithm completed with {} total matches.", matches.size());
        return matches;
    }


    private boolean isAlreadyMatched(Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches, ParticipantInProgrammeYear mentee) {
        boolean matched = matches.values().stream().anyMatch(list -> list.contains(mentee));
        if (matched) {
            log.debug("Mentee {} is already matched.", mentee.getId());
        } else {
            log.debug("Mentee {} is not yet matched.", mentee.getId());
        }
        return matched;
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
