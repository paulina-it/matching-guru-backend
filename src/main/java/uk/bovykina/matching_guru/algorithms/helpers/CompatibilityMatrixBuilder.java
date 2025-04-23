package uk.bovykina.matching_guru.algorithms.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.algorithms.interfaces.CompatibilityCalculator;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeMatchingCriteria;
import uk.bovykina.matching_guru.repository.ProgrammeMatchingCriteriaRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompatibilityMatrixBuilder {

    private final CompatibilityCalculator compatibilityCalculator;
    private final ProgrammeMatchingCriteriaRepository criteriaRepository;
    private final MentorshipValidator mentorshipValidator;

    public Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> build(
            List<ParticipantInProgrammeYear> mentors,
            List<ParticipantInProgrammeYear> mentees,
            Long programmeYearId,
            boolean strictStage,
            boolean strictGroup
    ) {
        Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> scores = new HashMap<>();
        Map<String, Integer> weights = loadWeights(programmeYearId);

        for (ParticipantInProgrammeYear mentor : mentors) {
            Map<ParticipantInProgrammeYear, Double> mentorScores = new HashMap<>();
            for (ParticipantInProgrammeYear mentee : mentees) {
                Long mentorId = mentor.getUser().getId();
                Long menteeId = mentee.getUser().getId();

                if (mentorId.equals(menteeId)) {
                    log.debug("⛔ Skipping self-match: Mentor {} and Mentee {}", mentorId, menteeId);
                    continue;
                }

                var menteePreference = mentee.getGenderPreference();
                var mentorGender = mentor.getUser().getGender();
                if (menteePreference != null &&
                        !menteePreference.name().equalsIgnoreCase("PREFER_NOT_TO_SAY") &&
                        (mentorGender == null || !menteePreference.equals(mentorGender))) {

                    log.debug("🚫 Skipping match due to mentee's gender preference: Mentee {} prefers {}, but Mentor {} is {}",
                            menteeId, menteePreference, mentorId, mentorGender);
                    continue;
                }

                var mentorPreference = mentor.getGenderPreference();
                var menteeGender = mentee.getUser().getGender();
                if (mentorPreference != null &&
                        !mentorPreference.name().equalsIgnoreCase("PREFER_NOT_TO_SAY") &&
                        (menteeGender == null || !mentorPreference.equals(menteeGender))) {

                    log.debug("🚫 Skipping match due to mentor's gender preference: Mentor {} prefers {}, but Mentee {} is {}",
                            mentorId, mentorPreference, menteeId, menteeGender);
                    continue;
                }

                if (!mentorshipValidator.isCompatible(mentor, mentee, strictStage, strictGroup)) {
                    log.debug("🚫 Mentor {} and Mentee {} failed structural compatibility check", mentorId, menteeId);
                    continue;
                }
                double score = compatibilityCalculator.calculate(mentor, mentee, weights);
                if (score > 0) {
                    mentorScores.put(mentee, score);
                    log.debug("✅ Match added: Mentor {} → Mentee {} (score = {})", mentorId, menteeId, score);
                } else {
                    log.debug("⚠ Match skipped: Mentor {} → Mentee {} (score = 0)", mentorId, menteeId);
                }
            }
            scores.put(mentor, mentorScores);
        }

        return scores;
    }

    private Map<String, Integer> loadWeights(Long programmeYearId) {
        return criteriaRepository.findByProgrammeYearId(programmeYearId).stream()
                .collect(Collectors.toMap(
                        c -> c.getCriterionType().name(),
                        ProgrammeMatchingCriteria::getWeight
                ));
    }
}
