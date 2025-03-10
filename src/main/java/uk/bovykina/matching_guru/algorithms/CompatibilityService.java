package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CompatibilityService {
    private final ProgrammeMatchingCriteriaRepository criteriaRepository;
    private final Map<String, Double> compatibilityCache = new ConcurrentHashMap<>();

    protected double calculateScore(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee, Map<String, Integer> weights) {
        if (!isValidMentorship(mentor.getAcademicStage(), mentee.getAcademicStage())) {
            return 0;
        }

        String cacheKey = mentor.getId() + "-" + mentee.getId();
        if (compatibilityCache.containsKey(cacheKey)) {
            return compatibilityCache.get(cacheKey);
        }

        double score = 0;
        int maxScore = weights.values().stream().mapToInt(Integer::intValue).sum();

        if (maxScore == 0) {
            log.warn("⚠ Weight sum is zero! Using default maxScore of 100.");
            maxScore = 100;
        }

        // **Field Matching (Course or Group)**
        boolean sameCourse = mentor.getCourse().getId().equals(mentee.getCourse().getId());
        boolean sameGroup = mentor.getCourseGroup().equals(mentee.getCourseGroup());

        if (sameCourse) {
            score += maxScore * 0.6;
        } else if (sameGroup) {
            score += maxScore * 0.5;
        }

        // **Availability (Common Available Days)**
        if (!Collections.disjoint(mentor.getAvailableDays(), mentee.getAvailableDays())) {
            score += weights.getOrDefault(CriterionType.AVAILABILITY.name(), 8);
        }

        // **Personality Type Compatibility (MBTI)**
        if (mentor.getUser().getPersonalityType() != null && mentee.getUser().getPersonalityType() != null) {
            score += getMBTICompatibilityScore(mentor.getUser().getPersonalityType(), mentee.getUser().getPersonalityType(), weights);
        }

        // **Skill Matching**
        Set<Skill> mentorSkills = mentor.getSkills();
        Set<Skill> menteeSkills = mentee.getSkills();
        long matchingSkills = mentorSkills.stream().filter(menteeSkills::contains).count();

        if (!mentorSkills.isEmpty() && !menteeSkills.isEmpty()) {
            double skillMatchRatio = (double) matchingSkills / Math.max(mentorSkills.size(), menteeSkills.size());
            score += weights.getOrDefault(CriterionType.SKILLS.name(), 5) * skillMatchRatio;
        }

        // **Same Gender Preference**
        if (mentor.getUser().getGender() != null && mentee.getUser().getGender() != null
                && mentor.getUser().getGender().equals(mentee.getUser().getGender())) {
            score += weights.getOrDefault(CriterionType.GENDER.name(), 3);
        }

        // **Close Age Groups**
        if (mentor.getUser().getAgeGroup() != null && mentee.getUser().getAgeGroup() != null
                && Math.abs(mentor.getUser().getAgeGroup().ordinal() - mentee.getUser().getAgeGroup().ordinal()) <= 2) {
            score += weights.getOrDefault(CriterionType.AGE.name(), 4);
        }

        // **Living Arrangement**
        if (mentor.getUser().getLivingArrangement() != null && mentee.getUser().getLivingArrangement() != null
                && Math.abs(mentor.getUser().getLivingArrangement().ordinal() - mentee.getUser().getLivingArrangement().ordinal()) <= 2) {
            score += weights.getOrDefault(CriterionType.LIVING_ARRANGEMENT.name(), 4);
        }

        double normalizedScore = (score / maxScore) * 100;
        compatibilityCache.put(cacheKey, normalizedScore);

        return Math.round(normalizedScore);
    }

    protected boolean needsApproval(double matchScore, ProgrammeYear programmeYear) {
        if (programmeYear.getMatchApprovalType() == MatchApprovalType.AUTO) {
            return false;
        }
        if (programmeYear.getMatchApprovalType() == MatchApprovalType.MANUAL) {
            return true;
        }
        if (programmeYear.getMatchApprovalType() == MatchApprovalType.THRESHOLD) {
            return matchScore < programmeYear.getApprovalThreshold();
        }
        return true;
    }

    protected boolean isValidMentorship(AcademicStage mentorStage, AcademicStage menteeStage) {
        Map<AcademicStage, List<AcademicStage>> validMentorships = Map.of(
                AcademicStage.FOUNDATION, List.of(AcademicStage.FIRST_YEAR),
                AcademicStage.FIRST_YEAR, List.of(AcademicStage.SECOND_YEAR),
                AcademicStage.SECOND_YEAR, List.of(AcademicStage.PLACEMENT, AcademicStage.FINAL_YEAR),
                AcademicStage.FINAL_YEAR, List.of(AcademicStage.PG_MASTERS, AcademicStage.PG_PHD)
        );

        return validMentorships.getOrDefault(menteeStage, List.of()).contains(mentorStage);
    }

    protected Map<String, Integer> loadMatchingCriteria(Long programmeYearId) {
        return criteriaRepository.findByProgrammeYearId(programmeYearId).stream()
                .collect(Collectors.toMap(
                        c -> c.getCriterionType().name(),
                        ProgrammeMatchingCriteria::getWeight
                ));
    }


    protected int getMBTICompatibilityScore(PersonalityType mentorType, PersonalityType menteeType, Map<String, Integer> weights) {
        if (mentorType == null || menteeType == null) {
            return 0;
        }

        Map<PersonalityType, List<PersonalityType>> mbtiBestMatches = new HashMap<>();
        mbtiBestMatches.put(PersonalityType.ARCHITECT_INTJ, List.of(PersonalityType.DEBATER_ENTP, PersonalityType.COMMANDER_ENTJ, PersonalityType.ADVOCATE_INFJ));
        mbtiBestMatches.put(PersonalityType.LOGICIAN_INTP, List.of(PersonalityType.CAMPAIGNER_ENFP, PersonalityType.PROTAGONIST_ENFJ, PersonalityType.ARCHITECT_INTJ));
        mbtiBestMatches.put(PersonalityType.COMMANDER_ENTJ, List.of(PersonalityType.ADVOCATE_INFJ, PersonalityType.DEBATER_ENTP, PersonalityType.EXECUTIVE_ESTJ));
        mbtiBestMatches.put(PersonalityType.DEBATER_ENTP, List.of(PersonalityType.ADVOCATE_INFJ, PersonalityType.COMMANDER_ENTJ, PersonalityType.ENTREPRENEUR_ESTP));

        mbtiBestMatches.put(PersonalityType.ADVOCATE_INFJ, List.of(PersonalityType.COMMANDER_ENTJ, PersonalityType.CAMPAIGNER_ENFP, PersonalityType.ADVOCATE_INFJ));
        mbtiBestMatches.put(PersonalityType.MEDIATOR_INFP, List.of(PersonalityType.PROTAGONIST_ENFJ, PersonalityType.CAMPAIGNER_ENFP, PersonalityType.DEFENDER_ISFJ));
        mbtiBestMatches.put(PersonalityType.PROTAGONIST_ENFJ, List.of(PersonalityType.MEDIATOR_INFP, PersonalityType.CAMPAIGNER_ENFP, PersonalityType.LOGICIAN_INTP));
        mbtiBestMatches.put(PersonalityType.CAMPAIGNER_ENFP, List.of(PersonalityType.ADVOCATE_INFJ, PersonalityType.LOGICIAN_INTP, PersonalityType.PROTAGONIST_ENFJ));

        mbtiBestMatches.put(PersonalityType.LOGISTICIAN_ISTJ, List.of(PersonalityType.DEFENDER_ISFJ, PersonalityType.EXECUTIVE_ESTJ, PersonalityType.LOGICIAN_INTP));
        mbtiBestMatches.put(PersonalityType.DEFENDER_ISFJ, List.of(PersonalityType.LOGISTICIAN_ISTJ, PersonalityType.MEDIATOR_INFP, PersonalityType.CONSUL_ESFJ));
        mbtiBestMatches.put(PersonalityType.EXECUTIVE_ESTJ, List.of(PersonalityType.COMMANDER_ENTJ, PersonalityType.LOGISTICIAN_ISTJ, PersonalityType.CONSUL_ESFJ));
        mbtiBestMatches.put(PersonalityType.CONSUL_ESFJ, List.of(PersonalityType.EXECUTIVE_ESTJ, PersonalityType.DEFENDER_ISFJ, PersonalityType.ENTERTAINER_ESFP));

        mbtiBestMatches.put(PersonalityType.VIRTUOSO_ISTP, List.of(PersonalityType.ENTREPRENEUR_ESTP, PersonalityType.ADVENTURER_ISFP, PersonalityType.LOGICIAN_INTP));
        mbtiBestMatches.put(PersonalityType.ADVENTURER_ISFP, List.of(PersonalityType.VIRTUOSO_ISTP, PersonalityType.ENTERTAINER_ESFP, PersonalityType.MEDIATOR_INFP));
        mbtiBestMatches.put(PersonalityType.ENTREPRENEUR_ESTP, List.of(PersonalityType.VIRTUOSO_ISTP, PersonalityType.DEBATER_ENTP, PersonalityType.EXECUTIVE_ESTJ));
        mbtiBestMatches.put(PersonalityType.ENTERTAINER_ESFP, List.of(PersonalityType.ADVENTURER_ISFP, PersonalityType.CONSUL_ESFJ, PersonalityType.CAMPAIGNER_ENFP));

        // Same approach for "Good Matches"
        Map<PersonalityType, List<PersonalityType>> mbtiGoodMatches = new HashMap<>();
        mbtiGoodMatches.put(PersonalityType.ARCHITECT_INTJ, List.of(PersonalityType.LOGICIAN_INTP, PersonalityType.LOGISTICIAN_ISTJ));
        mbtiGoodMatches.put(PersonalityType.LOGICIAN_INTP, List.of(PersonalityType.ARCHITECT_INTJ, PersonalityType.VIRTUOSO_ISTP));
        mbtiGoodMatches.put(PersonalityType.COMMANDER_ENTJ, List.of(PersonalityType.EXECUTIVE_ESTJ, PersonalityType.ADVOCATE_INFJ));
        mbtiGoodMatches.put(PersonalityType.DEBATER_ENTP, List.of(PersonalityType.ENTREPRENEUR_ESTP, PersonalityType.CAMPAIGNER_ENFP));

        mbtiGoodMatches.put(PersonalityType.ADVOCATE_INFJ, List.of(PersonalityType.ADVOCATE_INFJ, PersonalityType.COMMANDER_ENTJ));
        mbtiGoodMatches.put(PersonalityType.MEDIATOR_INFP, List.of(PersonalityType.PROTAGONIST_ENFJ, PersonalityType.ADVENTURER_ISFP));
        mbtiGoodMatches.put(PersonalityType.PROTAGONIST_ENFJ, List.of(PersonalityType.CAMPAIGNER_ENFP, PersonalityType.MEDIATOR_INFP));
        mbtiGoodMatches.put(PersonalityType.CAMPAIGNER_ENFP, List.of(PersonalityType.PROTAGONIST_ENFJ, PersonalityType.DEBATER_ENTP));

        mbtiGoodMatches.put(PersonalityType.LOGISTICIAN_ISTJ, List.of(PersonalityType.LOGICIAN_INTP, PersonalityType.EXECUTIVE_ESTJ));
        mbtiGoodMatches.put(PersonalityType.DEFENDER_ISFJ, List.of(PersonalityType.CONSUL_ESFJ, PersonalityType.EXECUTIVE_ESTJ));
        mbtiGoodMatches.put(PersonalityType.EXECUTIVE_ESTJ, List.of(PersonalityType.COMMANDER_ENTJ, PersonalityType.LOGISTICIAN_ISTJ));
        mbtiGoodMatches.put(PersonalityType.CONSUL_ESFJ, List.of(PersonalityType.ENTERTAINER_ESFP, PersonalityType.DEFENDER_ISFJ));

        mbtiGoodMatches.put(PersonalityType.VIRTUOSO_ISTP, List.of(PersonalityType.LOGICIAN_INTP, PersonalityType.ENTREPRENEUR_ESTP));
        mbtiGoodMatches.put(PersonalityType.ADVENTURER_ISFP, List.of(PersonalityType.ENTERTAINER_ESFP, PersonalityType.MEDIATOR_INFP));
        mbtiGoodMatches.put(PersonalityType.ENTREPRENEUR_ESTP, List.of(PersonalityType.DEBATER_ENTP, PersonalityType.VIRTUOSO_ISTP));
        mbtiGoodMatches.put(PersonalityType.ENTERTAINER_ESFP, List.of(PersonalityType.CONSUL_ESFJ, PersonalityType.ADVENTURER_ISFP));

        int basePersonalityScore = weights.getOrDefault(CriterionType.PERSONALITY.name(), 7);

        if (mbtiBestMatches.getOrDefault(mentorType, List.of()).contains(menteeType)) {
            return basePersonalityScore + 3;
        }
        if (mbtiGoodMatches.getOrDefault(mentorType, List.of()).contains(menteeType)) {
            return basePersonalityScore + 2;
        }

        return basePersonalityScore;
    }
}
