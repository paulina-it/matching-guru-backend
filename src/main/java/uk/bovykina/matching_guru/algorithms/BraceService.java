package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.match.MatchCreateDto;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeMatchingCriteria;
import uk.bovykina.matching_guru.entity.enums.*;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.ProgrammeMatchingCriteriaRepository;
import uk.bovykina.matching_guru.service.MatchService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BraceService {

    private final ParticipantRepository participantRepository;
    private final ProgrammeMatchingCriteriaRepository criteriaRepository;
    private final MatchService matchService;
    private final CompatibilityService compatibilityService;

    @Transactional
    public void matchParticipantsWithBrace(Long programmeYearId) {
        log.info("▶ Starting BRACE matching process for ProgrammeYear ID: {}", programmeYearId);

        List<ParticipantInProgrammeYear> mentors = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTOR);
        List<ParticipantInProgrammeYear> mentees = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTEE);

        log.info("👥 Found {} mentors and {} mentees", mentors.size(), mentees.size());

        if (mentors.isEmpty() || mentees.isEmpty()) {
            log.warn("⚠ Not enough participants for matching");
            return;
        }

        Map<String, Integer> weights = loadMatchingCriteria(programmeYearId);
        log.info("📊 Loaded criteria weights: {}", weights);

        // Initialize matches
        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches = new HashMap<>();

        // Step 1: Calculate compatibility scores for all pairs
        Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> compatibilityScores = new HashMap<>();
        for (ParticipantInProgrammeYear mentor : mentors) {
            Map<ParticipantInProgrammeYear, Double> mentorScores = new HashMap<>();
            for (ParticipantInProgrammeYear mentee : mentees) {
                double score = compatibilityService.calculateScore(mentor, mentee, weights);
                mentorScores.put(mentee, score);
            }
            compatibilityScores.put(mentor, mentorScores);
        }

        // Step 2: Iteratively find the best pairings
        boolean changesMade;
        do {
            changesMade = false;

            for (ParticipantInProgrammeYear mentee : mentees) {
                ParticipantInProgrammeYear bestMentor = findBestMentorForMentee(mentee, compatibilityScores, matches);

                if (bestMentor != null && (!matches.containsKey(mentee) || !matches.get(mentee).equals(bestMentor))) {
                    // Update the match for the mentee
                    matches.put(mentee, bestMentor);
                    changesMade = true;
                    log.info("🔗 Matched Mentee {} with Mentor {} based on compatibility score", mentee.getId(), bestMentor.getId());
                }
            }
        } while (changesMade); // Continue until no more changes are made

        // Step 3: Create matches in the database
        matches.forEach((mentee, mentor) -> {
            double compatibilityScore = compatibilityScores.get(mentor).get(mentee);
            log.info("🔗 Creating match: {} (mentor) → {} (mentee) with compatibility {}", mentor.getId(), mentee.getId(), compatibilityScore);

            MatchCreateDto matchCreateDto = new MatchCreateDto(programmeYearId, mentor.getId(), mentee.getId(), compatibilityScore);
            matchService.createMatch(matchCreateDto);

            mentor.setIsMatched(true);
            mentee.setIsMatched(true);

            participantRepository.save(mentor);
            participantRepository.save(mentee);

            log.info("✅ Match saved: {} (mentor) → {} (mentee) with compatibility {}", mentor.getId(), mentee.getId(), compatibilityScore);
        });

        log.info("✔ BRACE matching process completed for ProgrammeYear ID: {}", programmeYearId);
    }

    private ParticipantInProgrammeYear findBestMentorForMentee(ParticipantInProgrammeYear mentee,
                                                               Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> compatibilityScores,
                                                               Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> currentMatches) {

        // Find the best mentor for the mentee based on compatibility score
        ParticipantInProgrammeYear bestMentor = null;
        double bestScore = 0;

        for (Map.Entry<ParticipantInProgrammeYear, Double> entry : compatibilityScores.get(mentee.getCourse()).entrySet()) {
            ParticipantInProgrammeYear mentor = entry.getKey();
            double score = entry.getValue();

            // Ensure the mentor is not already matched or choose the best match for an unmatched mentee
            if ((bestMentor == null || score > bestScore) && (currentMatches.get(mentor) == null || !currentMatches.get(mentor).equals(mentee))) {
                bestMentor = mentor;
                bestScore = score;
            }
        }
        return bestMentor;
    }

    private Map<String, Integer> loadMatchingCriteria(Long programmeYearId) {
        return criteriaRepository.findByProgrammeYearId(programmeYearId).stream()
                .collect(Collectors.toMap(
                        c -> c.getCriterionType().name(),
                        ProgrammeMatchingCriteria::getWeight
                ));
    }
}
