package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.algorithms.helpers.MatchSaver;
import uk.bovykina.matching_guru.algorithms.helpers.MatchingCriteriaProvider;
import uk.bovykina.matching_guru.algorithms.interfaces.CompatibilityCalculator;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.service.ProgrammeYearService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CollaborativeFilteringService {

    private final ParticipantRepository participantRepository;
    private final ProgrammeYearService programmeYearService;
    private final MatchSaver matchSaver;
    private final MatchingCriteriaProvider criteriaProvider;

    @Transactional
    public void collaborativeFilteringMatch(Long programmeYearId) {
        List<ParticipantInProgrammeYear> mentors = participantRepository.findByProgrammeYearId(programmeYearId).stream()
                .filter(p -> p.getRole() == ParticipantRole.MENTOR)
                .toList();

        List<ParticipantInProgrammeYear> mentees = participantRepository.findByProgrammeYearId(programmeYearId).stream()
                .filter(p -> p.getRole() == ParticipantRole.MENTEE)
                .toList();

        if (mentors.isEmpty() || mentees.isEmpty()) return;

        ProgrammeYear programmeYear = programmeYearService.getById(programmeYearId);
        Map<String, Integer> weights = criteriaProvider.loadWeights(programmeYearId);

        Set<ParticipantInProgrammeYear> matchedMentees = new HashSet<>();
        Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> finalMatches = new HashMap<>();

        for (ParticipantInProgrammeYear mentor : mentors) {
            List<Double> mentorVector = getParticipantVector(mentor);

            ParticipantInProgrammeYear bestMentee = null;
            double bestSimilarity = 0;

            for (ParticipantInProgrammeYear mentee : mentees) {
                if (matchedMentees.contains(mentee)) continue;

                List<Double> menteeVector = getParticipantVector(mentee);
                double similarity = cosineSimilarity(mentorVector, menteeVector);

                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                    bestMentee = mentee;
                }
            }

            if (bestMentee != null) {
                finalMatches.put(mentor, List.of(bestMentee));
                matchedMentees.add(bestMentee);
            }
        }

        matchSaver.saveMatches(programmeYearId, programmeYear, finalMatches, weights);
    }

    private List<Double> getParticipantVector(ParticipantInProgrammeYear p) {
        return List.of(
                (double) p.getAcademicStage().ordinal(),
                (double) p.getSkills().size(),
                (double) p.getAvailableDays().size(),
                p.getCourseGroup() != null ? (double) p.getCourseGroup().getId() : 0.0
        );
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        double dot = 0, magA = 0, magB = 0;

        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            magA += Math.pow(a.get(i), 2);
            magB += Math.pow(b.get(i), 2);
        }

        double denominator = Math.sqrt(magA) * Math.sqrt(magB);
        return (denominator == 0) ? 0 : dot / denominator;
    }
}
