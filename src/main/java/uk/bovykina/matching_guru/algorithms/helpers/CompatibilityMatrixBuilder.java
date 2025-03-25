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
            Long programmeYearId
    ) {
        Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> scores = new HashMap<>();
        Map<String, Integer> weights = loadWeights(programmeYearId);

        for (ParticipantInProgrammeYear mentor : mentors) {
            Map<ParticipantInProgrammeYear, Double> mentorScores = new HashMap<>();
            for (ParticipantInProgrammeYear mentee : mentees) {
                if (!isPotentiallyCompatible(mentor, mentee)) continue;

                double score = compatibilityCalculator.calculate(mentor, mentee, weights);
                if (score > 0) mentorScores.put(mentee, score);
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

    private boolean isPotentiallyCompatible(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee) {
        return mentorshipValidator.isCompatible(mentor, mentee, true);
    }
}
