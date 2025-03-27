package uk.bovykina.matching_guru.algorithms.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchAssigner {
    private static final double MIN_SCORE = 0;
    private static final double FALLBACK_SCORE = 0;

    private final MentorshipValidator mentorshipValidator;

    Map<ParticipantInProgrammeYear, List<Map.Entry<ParticipantInProgrammeYear, Double>>> topMentors = new HashMap<>();

    public Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> assignMatches(
            List<ParticipantInProgrammeYear> mentors,
            List<ParticipantInProgrammeYear> mentees,
            Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> scores,
            ProgrammeYear programmeYear
    ) {
        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches = new HashMap<>();
        Map<ParticipantInProgrammeYear, Integer> mentorLoad = new HashMap<>();
        mentors.forEach(m -> mentorLoad.put(m, 0));

        mentees = new ArrayList<>(mentees);
        mentees.sort(Comparator.comparingInt(mentee ->
                (int) mentors.stream().filter(mentor -> scores.getOrDefault(mentor, Map.of()).containsKey(mentee)).count()
        ));

        long menteesWithAtLeastOneMentor = mentees.stream()
                .filter(mentee ->
                        mentors.stream().anyMatch(mentor ->
                                scores.getOrDefault(mentor, Map.of()).getOrDefault(mentee, 0.0) > 0
                        )
                ).count();

        log.info("🧠 {} out of {} mentees have at least one compatible mentor", menteesWithAtLeastOneMentor, mentees.size());

        match(mentees, mentors, scores, matches, mentorLoad, programmeYear, MIN_SCORE, false);

        List<ParticipantInProgrammeYear> unmatched = mentees.stream()
                .filter(m -> !matches.containsKey(m))
                .toList();

        match(unmatched, mentors, scores, matches, mentorLoad, programmeYear, FALLBACK_SCORE, true);

        long fullMentors = mentorLoad.entrySet().stream()
                .filter(e -> e.getValue() >= (e.getKey().getMenteesNumber() != null ? e.getKey().getMenteesNumber() : 1))
                .count();
        log.info("📦 {} mentors reached capacity", fullMentors);

        List<ParticipantInProgrammeYear> unmatchedMentees = mentees.stream()
                .filter(m -> !matches.containsKey(m))
                .toList();

        log.info("📋 Total unmatched mentees: {}", unmatchedMentees.size());

        File file = new File("exports/unmatched_mentees.csv");
        file.getParentFile().mkdirs();
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("menteeId,compatibleMentors");

            for (ParticipantInProgrammeYear mentee : mentees) {
                if (!matches.containsKey(mentee)) {
                    long compatibleCount = mentors.stream()
                            .filter(mentor -> scores.getOrDefault(mentor, Map.of()).getOrDefault(mentee, 0.0) > 0)
                            .count();
                    out.printf("%d,%d%n", mentee.getId(), compatibleCount);
                }
            }
            log.info("📝 Exported unmatched mentees to {}", file.getAbsolutePath());
        } catch (Exception e) {
            log.error("❌ Failed to write unmatched mentees log: {}", e.getMessage());
        }

        log.info("✅ Summary: {} matches created out of {} mentees", matches.size(), mentees.size());

        return matches;
    }

    private void match(List<ParticipantInProgrammeYear> mentees,
                       List<ParticipantInProgrammeYear> mentors,
                       Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> scores,
                       Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches,
                       Map<ParticipantInProgrammeYear, Integer> mentorLoad,
                       ProgrammeYear programmeYear,
                       double minScore,
                       boolean isFallbackPhase) {

        topMentors.clear();

        boolean strictStage = !isFallbackPhase && Boolean.TRUE.equals(programmeYear.getStrictAcademicStage());
        boolean strictCourseGroup = !isFallbackPhase && Boolean.TRUE.equals(programmeYear.getStrictCourseGroup());

        for (ParticipantInProgrammeYear mentee : mentees) {
            ParticipantInProgrammeYear bestMentor = null;
            double bestScore = 0;

            List<ParticipantInProgrammeYear> shuffledMentors = new ArrayList<>(mentors);
            Collections.shuffle(shuffledMentors);

            for (ParticipantInProgrammeYear mentor : shuffledMentors) {
                if (mentee.getUser().getId().equals(mentor.getUser().getId())) continue;

                int allowed = mentor.getMenteesNumber() != null ? mentor.getMenteesNumber() : 1;
                if (mentorLoad.getOrDefault(mentor, 0) >= allowed) continue;

                if (!mentorshipValidator.isCompatible(mentor, mentee, strictStage, strictCourseGroup)) continue;

                double score = scores.getOrDefault(mentor, Map.of()).getOrDefault(mentee, 0.0);
                if (score >= minScore) {
                    topMentors.computeIfAbsent(mentee, k -> new ArrayList<>()).add(
                            new AbstractMap.SimpleEntry<>(mentor, score)
                    );
                }
            }

            if (topMentors.containsKey(mentee)) {
                List<Map.Entry<ParticipantInProgrammeYear, Double>> ranked = new ArrayList<>(topMentors.get(mentee));
                ranked.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
                List<Map.Entry<ParticipantInProgrammeYear, Double>> topN = new ArrayList<>(
                        ranked.subList(0, Math.min(3, ranked.size()))
                );
                Collections.shuffle(topN);
                Map.Entry<ParticipantInProgrammeYear, Double> selected = topN.get(0);

                bestMentor = selected.getKey();
                bestScore = selected.getValue();
            }

            if (bestMentor != null) {
                matches.put(mentee, bestMentor);
                mentorLoad.put(bestMentor, mentorLoad.get(bestMentor) + 1);
                if (isFallbackPhase) {
                    log.info("🟡 Fallback match: Mentee {} → Mentor {} (score = {})", mentee.getId(), bestMentor.getId(), bestScore);
                } else {
                    log.info("🔵 Primary match: Mentee {} → Mentor {} (score = {})", mentee.getId(), bestMentor.getId(), bestScore);
                }
            } else {
                log.warn("❌ No match found for Mentee {}", mentee.getId());
            }
        }
    }
}
