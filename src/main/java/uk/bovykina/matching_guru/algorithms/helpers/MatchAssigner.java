package uk.bovykina.matching_guru.algorithms.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MatchAssigner {
    private static final double MIN_SCORE = 30;
    private static final double FALLBACK_SCORE = 20;

    public Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> assignMatches(
            List<ParticipantInProgrammeYear> mentors,
            List<ParticipantInProgrammeYear> mentees,
            Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> scores
    ) {
        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches = new HashMap<>();
        Map<ParticipantInProgrammeYear, Integer> mentorLoad = new HashMap<>();
        mentors.forEach(m -> mentorLoad.put(m, 0));

        mentees.sort(Comparator.comparingInt(mentee ->
                (int) mentors.stream().filter(mentor -> scores.getOrDefault(mentor, Map.of()).containsKey(mentee)).count()
        ));

        // Phase 1
        match(mentees, mentors, scores, matches, mentorLoad, MIN_SCORE);

        // Phase 2 - fallback
        List<ParticipantInProgrammeYear> unmatched = mentees.stream().filter(m -> !matches.containsKey(m)).toList();
        match(unmatched, mentors, scores, matches, mentorLoad, FALLBACK_SCORE);

        return matches;
    }

    private void match(List<ParticipantInProgrammeYear> mentees,
                       List<ParticipantInProgrammeYear> mentors,
                       Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> scores,
                       Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches,
                       Map<ParticipantInProgrammeYear, Integer> mentorLoad,
                       double minScore) {

        for (ParticipantInProgrammeYear mentee : mentees) {
            ParticipantInProgrammeYear bestMentor = null;
            double bestScore = 0;

            for (ParticipantInProgrammeYear mentor : mentors) {int allowed = mentor.getMenteesNumber() != null ? mentor.getMenteesNumber() : 1;
                if (mentorLoad.getOrDefault(mentor, 0) >= allowed) continue;


                double score = scores.getOrDefault(mentor, Map.of()).getOrDefault(mentee, 0.0);
                if (score >= minScore && score > bestScore) {
                    bestMentor = mentor;
                    bestScore = score;
                }
            }

            if (bestMentor != null) {
                matches.put(mentee, bestMentor);
                mentorLoad.put(bestMentor, mentorLoad.get(bestMentor) + 1);
                log.info("🔗 Mentee {} matched with Mentor {} (score = {})", mentee.getId(), bestMentor.getId(), bestScore);
            } else {
                log.warn("❌ No match found for Mentee {}", mentee.getId());
            }
        }
    }
}
