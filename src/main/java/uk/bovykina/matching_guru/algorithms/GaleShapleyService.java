package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.match.MatchCreateDto;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.entity.enums.*;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.ProgrammeMatchingCriteriaRepository;
import uk.bovykina.matching_guru.service.MatchService;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaleShapleyService {

    private final ParticipantRepository participantRepository;
    private final ProgrammeMatchingCriteriaRepository criteriaRepository;
    private final MatchService matchService;

    private Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> galeShapley(
            List<ParticipantInProgrammeYear> mentors,
            List<ParticipantInProgrammeYear> mentees,
            Map<String, Integer> weights
    ) {
        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches = new HashMap<>();
        Map<ParticipantInProgrammeYear, Queue<ParticipantInProgrammeYear>> mentorPreferences = new HashMap<>();

        // Предварительно вычисляем и фильтруем предпочтения для каждого ментора
        for (ParticipantInProgrammeYear mentor : mentors) {
            List<ParticipantInProgrammeYear> compatibleMentees = mentees.stream()
                    .filter(mentee -> isCompatible(mentor, mentee))
                    .sorted(Comparator.comparingDouble(mentee -> -calculateScore(mentor, mentee, weights)))
                    .toList();

            if (!compatibleMentees.isEmpty()) {
                mentorPreferences.put(mentor, new LinkedList<>(compatibleMentees));
            }
        }

        Queue<ParticipantInProgrammeYear> freeMentors = new LinkedList<>(mentorPreferences.keySet());
        log.info("🔄 Запуск алгоритма Gale-Shapley с {} активных менторов", freeMentors.size());

        while (!freeMentors.isEmpty()) {
            ParticipantInProgrammeYear mentor = freeMentors.poll();
            Queue<ParticipantInProgrammeYear> preferences = mentorPreferences.get(mentor);

            if (preferences.isEmpty()) {
                log.info("ℹ Ментор {} не имеет подходящих менти", mentor.getId());
                continue;
            }

            ParticipantInProgrammeYear mentee = preferences.poll();

            if (!matches.containsValue(mentee)) {
                matches.put(mentor, mentee);
                log.info("🔗 Создана пара: {} (ментор) → {} (менти)", mentor.getId(), mentee.getId());
            } else {
                // Если менти уже в паре, проверяем, предпочтет ли он нового ментора
                ParticipantInProgrammeYear currentMentor = matches.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(mentee))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElseThrow();

                if (calculateScore(mentor, mentee, weights) > calculateScore(currentMentor, mentee, weights)) {
                    matches.remove(currentMentor);
                    matches.put(mentor, mentee);
                    freeMentors.add(currentMentor);
                    log.info("🔄 Пара переназначена: {} (новый ментор) → {} (менти)", mentor.getId(), mentee.getId());
                } else if (!preferences.isEmpty()) {
                    freeMentors.add(mentor);
                }
            }
        }

        return matches;
    }

    private boolean isCompatible(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee) {
        // Проверка академических стадий
        if (!isValidMentorship(mentor.getAcademicStage(), mentee.getAcademicStage())) {
            log.debug("❌ Несовместимые академические стадии: {} (ментор) и {} (менти)",
                    mentor.getId(), mentee.getId());
            return false;
        }

        // Проверка курса или группы
        boolean sameCourse = mentor.getCourse().getId().equals(mentee.getCourse().getId());
        boolean sameGroup = mentor.getCourseGroup().equals(mentee.getCourseGroup());

        if (!sameCourse && !sameGroup) {
            log.debug("❌ Разные курсы и группы: {} (ментор) и {} (менти)",
                    mentor.getId(), mentee.getId());
            return false;
        }

        return true;
    }
    @Transactional
    public void matchParticipants(Long programmeYearId, boolean isInitialMatching) {
        log.info("▶ Начало процесса матчинга для ProgrammeYear ID: {} | Первичный: {}", programmeYearId, isInitialMatching);

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

        log.info("👥 Найдено {} менторов и {} менти", mentors.size(), mentees.size());

        if (mentors.isEmpty() || mentees.isEmpty()) {
            log.warn("⚠ Недостаточно участников для матчинга");
            return;
        }

        Map<String, Integer> weights = loadMatchingCriteria(programmeYearId);
        log.info("📊 Загружены веса критериев: {}", weights);

        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches = galeShapley(mentors, mentees, weights);

        matches.forEach((mentor, mentee) -> {
            double compatibilityScore = calculateScore(mentor, mentee, weights);
            log.info("🔗 Создание матча: {} (ментор) → {} (менти) с совместимостью {}", mentor.getId(), mentee.getId(), compatibilityScore);

            MatchCreateDto matchCreateDto = new MatchCreateDto(programmeYearId, mentor.getId(), mentee.getId(), compatibilityScore);
            matchService.createMatch(matchCreateDto);

            mentor.setIsMatched(true);
            mentee.setIsMatched(true);

            participantRepository.save(mentor);
            participantRepository.save(mentee);

            log.info("✅ Матч сохранен: {} (ментор) → {} (менти) с совместимостью {}", mentor.getId(), mentee.getId(), compatibilityScore);
        });

        log.info("✔ Матчинг завершён для ProgrammeYear ID: {}", programmeYearId);
    }

    private double calculateScore(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee, Map<String, Integer> weights) {
        if (!isValidMentorship(mentor.getAcademicStage(), mentee.getAcademicStage())) {
            return 0;  // Prevent invalid matches from getting a score.
        }

        double score = 0;
        int maxScore = weights.values().stream().mapToInt(Integer::intValue).sum();

        if (maxScore == 0) {
            log.warn("⚠ Weight sum is zero! Using default maxScore of 100.");
            maxScore = 100; // Prevent division by zero.
        }

        // ✅ **Field Matching (Course or Group)**
        if (mentor.getCourse().getId().equals(mentee.getCourse().getId())) {
            score += weights.getOrDefault(CriterionType.FIELD.name(), 10);
        }

        // ✅ **Availability (Common Available Days)**
        if (!Collections.disjoint(mentor.getAvailableDays(), mentee.getAvailableDays())) {
            score += weights.getOrDefault(CriterionType.AVAILABILITY.name(), 8);
        }

        // ✅ **Personality Type Compatibility (MBTI)**
        if (mentor.getUser().getPersonalityType() != null && mentee.getUser().getPersonalityType() != null) {
            score += getMBTICompatibilityScore(mentor.getUser().getPersonalityType(), mentee.getUser().getPersonalityType(), weights);
        }

        // ✅ **Skill Matching (Normalized)**
        Set<Skill> mentorSkills = mentor.getSkills();
        Set<Skill> menteeSkills = mentee.getSkills();
        long matchingSkills = mentorSkills.stream().filter(menteeSkills::contains).count();

        if (!mentorSkills.isEmpty() && !menteeSkills.isEmpty()) {
            double skillMatchRatio = (double) matchingSkills / Math.max(mentorSkills.size(), menteeSkills.size());
            score += weights.getOrDefault(CriterionType.SKILLS.name(), 5) * skillMatchRatio;
        }

        // ✅ **Same Gender Preference**
        if (mentor.getUser().getGender() != null && mentee.getUser().getGender() != null
                && mentor.getUser().getGender().equals(mentee.getUser().getGender())) {
            score += weights.getOrDefault(CriterionType.GENDER.name(), 3);
        }

        // ✅ **Close Age Groups (Within 2 Groups)**
        if (mentor.getUser().getAgeGroup() != null && mentee.getUser().getAgeGroup() != null
                && Math.abs(mentor.getUser().getAgeGroup().ordinal() - mentee.getUser().getAgeGroup().ordinal()) <= 2) {
            score += weights.getOrDefault(CriterionType.AGE.name(), 4);
        }

        // ✅ **Nationality Matching (Optional)**
//        if (mentor.getUser().getNationality() != null && mentee.getUser().getNationality() != null
//                && mentor.getUser().getNationality().equals(mentee.getUser().getNationality())) {
//            score += weights.getOrDefault(CriterionType.NATIONALITY.name(), 6);
//        }

        // 🔹 **Normalize Score to 0-100 Scale**
        double normalizedScore = (score / maxScore) * 100;
        return Math.round(normalizedScore);
    }


    private boolean isValidMentorship(AcademicStage mentorStage, AcademicStage menteeStage) {
        Map<AcademicStage, List<AcademicStage>> validMentorships = Map.of(
                AcademicStage.FOUNDATION, List.of(AcademicStage.FIRST_YEAR),
                AcademicStage.FIRST_YEAR, List.of(AcademicStage.SECOND_YEAR),
                AcademicStage.SECOND_YEAR, List.of(AcademicStage.PLACEMENT, AcademicStage.FINAL_YEAR),
                AcademicStage.FINAL_YEAR, List.of(AcademicStage.PG_MASTERS, AcademicStage.PG_PHD)
        );

        return validMentorships.getOrDefault(menteeStage, List.of()).contains(mentorStage);
    }

    private Map<String, Integer> loadMatchingCriteria(Long programmeYearId) {
        return criteriaRepository.findByProgrammeYearId(programmeYearId).stream()
                .collect(Collectors.toMap(
                        c -> c.getCriterionType().name(),
                        ProgrammeMatchingCriteria::getWeight
                ));
    }
    private int getMBTICompatibilityScore(PersonalityType mentorType, PersonalityType menteeType, Map<String, Integer> weights) {
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