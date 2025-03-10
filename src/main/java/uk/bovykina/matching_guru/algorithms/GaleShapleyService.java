package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.ProgrammeMatchingCriteriaRepository;
import uk.bovykina.matching_guru.service.MatchService;
import uk.bovykina.matching_guru.service.ProgrammeYearService;
import uk.bovykina.matching_guru.dto.match.MatchCreateDto;
import uk.bovykina.matching_guru.entity.ProgrammeMatchingCriteria;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaleShapleyService {

    private final ParticipantRepository participantRepository;
    private final MatchService matchService;
    private final CompatibilityService compatibilityService;
    private final ProgrammeMatchingCriteriaRepository criteriaRepository;
    private final ProgrammeYearService programmeYearService;

    private final Map<String, Double> compatibilityCache = new ConcurrentHashMap<>();

    private Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> galeShapley(
            List<ParticipantInProgrammeYear> mentors,
            List<ParticipantInProgrammeYear> mentees,
            Map<String, Integer> weights
    ) {
        Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches = new ConcurrentHashMap<>();
        Map<ParticipantInProgrammeYear, Queue<ParticipantInProgrammeYear>> mentorPreferences = new HashMap<>();

        for (ParticipantInProgrammeYear mentor : mentors) {
            List<ParticipantInProgrammeYear> compatibleMentees = mentees.stream()
                    .filter(mentee -> isCompatible(mentor, mentee))
                    .sorted(Comparator.comparingDouble(mentee -> -compatibilityService.calculateScore(mentor, mentee, weights)))
                    .toList();

            if (!compatibleMentees.isEmpty()) {
                mentorPreferences.put(mentor, new LinkedList<>(compatibleMentees));
            }
        }

        Queue<ParticipantInProgrammeYear> freeMentors = new LinkedList<>(mentorPreferences.keySet());
        log.info("🔄 Starting Gale-Shapley algorithm with {} active mentors", freeMentors.size());

        while (!freeMentors.isEmpty()) {
            ParticipantInProgrammeYear mentor = freeMentors.poll();
            Queue<ParticipantInProgrammeYear> preferences = mentorPreferences.get(mentor);

            if (preferences.isEmpty()) {
                log.info("ℹ Mentor {} has no suitable mentees", mentor.getId());
                continue;
            }

            while (!preferences.isEmpty()) {
                ParticipantInProgrammeYear mentee = preferences.poll();

                double compatibilityScore = compatibilityService.calculateScore(mentor, mentee, weights);

                if (compatibilityScore == 0) {
                    log.debug("❌ Mentor {} and Mentee {} have a compatibility score of 0. Skipping match.", mentor.getId(), mentee.getId());
                    continue;
                }

                if (!matches.containsValue(mentee)) {
                    matches.computeIfAbsent(mentor, k -> new ArrayList<>()).add(mentee);
                    log.info("🔗 Created a match: {} (mentor) → {} (mentee) with compatibility {}", mentor.getId(), mentee.getId(), compatibilityScore);
                    break;
                } else {
                    ParticipantInProgrammeYear currentMentor = matches.entrySet().stream()
                            .filter(entry -> entry.getValue().contains(mentee))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElseThrow();

                    if (compatibilityService.calculateScore(mentor, mentee, weights) > compatibilityService.calculateScore(currentMentor, mentee, weights)) {
                        matches.get(currentMentor).remove(mentee);
                        matches.computeIfAbsent(mentor, k -> new ArrayList<>()).add(mentee);
                        freeMentors.add(currentMentor);
                        log.info("🔄 Pair reassigned: {} (new mentor) → {} (mentee)", mentor.getId(), mentee.getId());
                        break;
                    }
                }
            }
        }

        return matches;
    }

    private boolean isCompatible(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee) {
        if (!compatibilityService.isValidMentorship(mentor.getAcademicStage(), mentee.getAcademicStage())) {
            log.debug("❌ Incompatible academic stages: {} (mentor) and {} (mentee)",
                    mentor.getId(), mentee.getId());
            return false;
        }

        boolean sameCourse = mentor.getCourse().getId().equals(mentee.getCourse().getId());
        boolean sameGroup = mentor.getCourseGroup().equals(mentee.getCourseGroup());

        if (sameCourse || sameGroup) {
            return true;
        }

        log.debug("❌ Different course groups: {} (mentor) and {} (mentee)", mentor.getId(), mentee.getId());
        return false;
    }

    @Transactional
    public void matchParticipants(Long programmeYearId, boolean isInitialMatching) {
        log.info("▶ Starting matching process for ProgrammeYear ID: {} | Initial: {}", programmeYearId, isInitialMatching);

        ProgrammeYear programmeYear = programmeYearService.getById(programmeYearId);
        List<ParticipantInProgrammeYear> mentors;
        List<ParticipantInProgrammeYear> mentees;

        if (isInitialMatching) {
            mentors = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTOR);
            mentees = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTEE);
        } else {
            mentors = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTOR)
                    .stream().filter(m -> !m.getIsMatched()).toList();

            mentees = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTEE)
                    .stream().filter(m -> !m.getIsMatched()).toList();
        }

        log.info("👥 Found {} mentors and {} mentees", mentors.size(), mentees.size());

        if (mentors.isEmpty() || mentees.isEmpty()) {
            log.warn("⚠ Not enough participants for matching");
            return;
        }

        Map<String, Integer> weights = loadMatchingCriteria(programmeYearId);
        log.info("📊 Loaded criteria weights: {}", weights);

        Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches = galeShapley(mentors, mentees, weights);

        // Create matches for each mentor and mentee
        matches.forEach((mentor, menteesList) -> {
            for (ParticipantInProgrammeYear mentee : menteesList) {
                double compatibilityScore = compatibilityService.calculateScore(mentor, mentee, weights);
                log.info("🔗 Creating match: {} (mentor) → {} (mentee) with compatibility {}", mentor.getId(), mentee.getId(), compatibilityScore);

                MatchStatus matchStatus = compatibilityService.needsApproval(compatibilityScore, programmeYear)
                        ? MatchStatus.PENDING
                        : MatchStatus.APPROVED;

                MatchCreateDto matchCreateDto = new MatchCreateDto(programmeYearId, mentor.getId(), mentee.getId(), compatibilityScore, matchStatus);
                matchService.createMatch(matchCreateDto);

                mentor.setIsMatched(true);
                mentee.setIsMatched(true);

                participantRepository.save(mentor);
                participantRepository.save(mentee);

                log.info("✅ Match saved: {} (mentor) → {} (mentee) with compatibility {}", mentor.getId(), mentee.getId(), compatibilityScore);
            }
        });

        log.info("✔ Matching process completed for ProgrammeYear ID: {}", programmeYearId);
    }

    private Map<String, Integer> loadMatchingCriteria(Long programmeYearId) {
        return criteriaRepository.findByProgrammeYearId(programmeYearId).stream()
                .collect(Collectors.toMap(
                        c -> c.getCriterionType().name(),
                        ProgrammeMatchingCriteria::getWeight
                ));
    }
}
