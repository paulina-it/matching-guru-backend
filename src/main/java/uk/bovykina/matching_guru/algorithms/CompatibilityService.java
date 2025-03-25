package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.algorithms.helpers.MBTICompatibilityService;
import uk.bovykina.matching_guru.algorithms.helpers.MentorshipValidator;
import uk.bovykina.matching_guru.algorithms.interfaces.CompatibilityCalculator;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeMatchingCriteria;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.AcademicStage;
import uk.bovykina.matching_guru.entity.enums.CriterionType;
import uk.bovykina.matching_guru.entity.enums.PersonalityType;
import uk.bovykina.matching_guru.entity.enums.Skill;
import uk.bovykina.matching_guru.entity.enums.MatchApprovalType;
import uk.bovykina.matching_guru.repository.ProgrammeMatchingCriteriaRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class CompatibilityService implements CompatibilityCalculator {

    private final MentorshipValidator mentorshipValidator;
    private final MBTICompatibilityService mbtiService;

    private final Map<String, Double> compatibilityCache = new ConcurrentHashMap<>();

    @Override
    public double calculate(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee, Map<String, Integer> weights) {
        if (!mentorshipValidator.isValid(mentor.getAcademicStage(), mentee.getAcademicStage())) return 0;

        String cacheKey = mentor.getId() + "-" + mentee.getId();
        if (compatibilityCache.containsKey(cacheKey)) {
            return compatibilityCache.get(cacheKey);
        }

        int maxScore = weights.values().stream().mapToInt(Integer::intValue).sum();
        if (maxScore == 0) {
            log.warn("⚠ No weights defined, defaulting to maxScore 100");
            maxScore = 100;
        }

        double score = 0;

        // ✅ Course/Group Match
        boolean sameCourse = mentor.getCourse() != null && mentee.getCourse() != null &&
                mentor.getCourse().getId().equals(mentee.getCourse().getId());
        boolean sameGroup = mentor.getCourseGroup() != null && mentee.getCourseGroup() != null &&
                mentor.getCourseGroup().getId().equals(mentee.getCourseGroup().getId());

        if (sameCourse) {
            score += maxScore * 0.6;
        } else if (sameGroup) {
            score += maxScore * 0.5;
        }

        // ✅ Availability
        if (!Collections.disjoint(mentor.getAvailableDays(), mentee.getAvailableDays())) {
            score += weights.getOrDefault(CriterionType.AVAILABILITY.name(), 8);
        }

        // ✅ MBTI
        if (mentor.getUser().getPersonalityType() != null && mentee.getUser().getPersonalityType() != null) {
            score += mbtiService.getScore(mentor.getUser().getPersonalityType(), mentee.getUser().getPersonalityType(), weights);
        }

        // ✅ Skill Match
        Set<Skill> mentorSkills = mentor.getSkills();
        Set<Skill> menteeSkills = mentee.getSkills();
        long matches = mentorSkills.stream().filter(menteeSkills::contains).count();
        if (!mentorSkills.isEmpty() && !menteeSkills.isEmpty()) {
            double ratio = (double) matches / Math.max(mentorSkills.size(), menteeSkills.size());
            score += weights.getOrDefault(CriterionType.SKILLS.name(), 5) * ratio;
        }

        // ✅ Gender
        if (mentor.getUser().getGender() != null && mentor.getUser().getGender().equals(mentee.getUser().getGender())) {
            score += weights.getOrDefault(CriterionType.GENDER.name(), 3);
        }

        // ✅ Age
        if (mentor.getUser().getAgeGroup() != null && mentee.getUser().getAgeGroup() != null) {
            if (Math.abs(mentor.getUser().getAgeGroup().ordinal() - mentee.getUser().getAgeGroup().ordinal()) <= 2) {
                score += weights.getOrDefault(CriterionType.AGE.name(), 4);
            }
        }

        // ✅ Living Arrangement
        if (mentor.getUser().getLivingArrangement() != null && mentee.getUser().getLivingArrangement() != null) {
            if (Math.abs(mentor.getUser().getLivingArrangement().ordinal() - mentee.getUser().getLivingArrangement().ordinal()) <= 2) {
                score += weights.getOrDefault(CriterionType.LIVING_ARRANGEMENT.name(), 4);
            }
        }

        double normalized = Math.min((score / maxScore) * 100, 100);
        double rounded = Math.round(normalized);
        compatibilityCache.put(cacheKey, rounded);
        return rounded;
    }
}
