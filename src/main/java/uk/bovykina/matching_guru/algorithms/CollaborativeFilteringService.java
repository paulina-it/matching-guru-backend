package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.service.MatchService;
import uk.bovykina.matching_guru.dto.match.MatchCreateDto;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CollaborativeFilteringService {

    private final ParticipantRepository participantRepository;
    private final MatchService matchService;
    private final CompatibilityService compatibilityService;

    public static double cosineSimilarity(List<Double> mentorVector, List<Double> menteeVector) {
        double dotProduct = 0.0;
        double mentorMagnitude = 0.0;
        double menteeMagnitude = 0.0;

        for (int i = 0; i < mentorVector.size(); i++) {
            dotProduct += mentorVector.get(i) * menteeVector.get(i);
            mentorMagnitude += Math.pow(mentorVector.get(i), 2);
            menteeMagnitude += Math.pow(menteeVector.get(i), 2);
        }

        mentorMagnitude = Math.sqrt(mentorMagnitude);
        menteeMagnitude = Math.sqrt(menteeMagnitude);

        return dotProduct / (mentorMagnitude * menteeMagnitude);
    }

    public List<Double> getParticipantVector(ParticipantInProgrammeYear participant) {
        List<Double> vector = new ArrayList<>();

        vector.add((double) participant.getAcademicStage().ordinal());
        vector.add(participant.getSkills().size() * 1.0);
        vector.add(participant.getAvailableDays().size() * 1.0);
        vector.add((double) participant.getCourseGroup().getId());

        return vector;
    }

    public void collaborativeFilteringMatch(Long programmeYearId) {
        List<ParticipantInProgrammeYear> mentors = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTOR);
        List<ParticipantInProgrammeYear> mentees = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTEE);

        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches = new HashMap<>();

        for (ParticipantInProgrammeYear mentor : mentors) {
            List<Double> mentorVector = getParticipantVector(mentor);

            ParticipantInProgrammeYear bestMatch = null;
            double highestSimilarity = 0;

            for (ParticipantInProgrammeYear mentee : mentees) {
                List<Double> menteeVector = getParticipantVector(mentee);

                double similarity = cosineSimilarity(mentorVector, menteeVector);

                if (similarity > highestSimilarity) {
                    highestSimilarity = similarity;
                    bestMatch = mentee;
                }
            }

            if (bestMatch != null) {
                matches.put(mentor, bestMatch);
            }
        }

        for (Map.Entry<ParticipantInProgrammeYear, ParticipantInProgrammeYear> entry : matches.entrySet()) {
            ParticipantInProgrammeYear mentor = entry.getKey();
            ParticipantInProgrammeYear mentee = entry.getValue();

            double compatibilityScore = calculateScore(mentor, mentee);
            MatchCreateDto matchCreateDto = new MatchCreateDto(programmeYearId, mentor.getId(), mentee.getId(), compatibilityScore);
            matchService.createMatch(matchCreateDto);

            mentor.setIsMatched(true);
            mentee.setIsMatched(true);

            participantRepository.save(mentor);
            participantRepository.save(mentee);
        }
    }

    private double calculateScore(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee) {
        Map<String, Integer> weights = compatibilityService.loadMatchingCriteria(mentor.getProgrammeYear().getId());

        return compatibilityService.calculateScore(mentor, mentee, weights);
    }
}
