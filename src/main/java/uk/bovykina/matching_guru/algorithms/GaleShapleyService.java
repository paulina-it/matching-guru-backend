package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.match.MatchCreateDto;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.entity.enums.AcademicStage;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
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

        log.info("👥 Найдено {} менторов и {} менторов", mentors.size(), mentees.size());

        if (mentors.isEmpty() || mentees.isEmpty()) {
            log.warn("⚠ Недостаточно участников для матчинга");
            return;
        }

        // Загружаем веса критериев
        Map<String, Integer> weights = loadMatchingCriteria(programmeYearId);
        log.info("📊 Загружены веса критериев: {}", weights);

        // Запуск алгоритма Gale-Shapley
        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches = galeShapley(mentors, mentees, weights);

        // Сохранение результатов через MatchService
        for (Map.Entry<ParticipantInProgrammeYear, ParticipantInProgrammeYear> entry : matches.entrySet()) {
            ParticipantInProgrammeYear mentor = entry.getKey();
            ParticipantInProgrammeYear mentee = entry.getValue();

            log.info("🔗 Предложено соответствие: {} (ментор) → {} (менти)", mentor.getId(), mentee.getId());

            MatchCreateDto matchCreateDto = new MatchCreateDto(mentor.getId(), mentee.getId());
            matchService.createMatch(matchCreateDto);

            mentor.setIsMatched(true);
            mentee.setIsMatched(true);

            participantRepository.save(mentor);
            participantRepository.save(mentee);

            log.info("✅ Совпадение сохранено: {} (ментор) → {} (менти)", mentor.getId(), mentee.getId());
        }

        log.info("✔ Матчинг завершён для ProgrammeYear ID: {}", programmeYearId);
    }

    private Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> galeShapley(
            List<ParticipantInProgrammeYear> mentors,
            List<ParticipantInProgrammeYear> mentees,
            Map<String, Integer> weights
    ) {
        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches = new HashMap<>();
        Queue<ParticipantInProgrammeYear> freeMentors = new LinkedList<>(mentors);

        log.info("🔄 Запуск алгоритма Gale-Shapley с {} менторов и {} менти", mentors.size(), mentees.size());

        while (!freeMentors.isEmpty()) {
            ParticipantInProgrammeYear mentor = freeMentors.poll();
            List<ParticipantInProgrammeYear> rankedMentees = sortParticipantsByScore(mentor, mentees, weights);

            for (ParticipantInProgrammeYear mentee : rankedMentees) {
                if (!matches.containsValue(mentee)) {
                    matches.put(mentor, mentee);
                    log.info("🔗 Связка предложена: {} (ментор) → {} (менти)", mentor.getId(), mentee.getId());
                    break;
                }
            }
        }

        return matches;
    }

    public double calculateScore(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee, Map<String, Integer> weights) {
        double score = 0;

        if (!isValidMentorship(mentor.getAcademicStage(), mentee.getAcademicStage())) {
            log.info("❌ Отклонение: {} (ментор) и {} (менти) — неподходящие академические стадии", mentor.getId(), mentee.getId());
            return -1;
        }

        if (mentor.getCourse().getId().equals(mentee.getCourse().getId())) {
            score += weights.getOrDefault("courseMatch", 10);
            log.info("🎓 Совпадение курса: {} (ментор) и {} (менти)", mentor.getId(), mentee.getId());
        } else if (mentor.getCourseGroup().equals(mentee.getCourseGroup())) {
            score += weights.getOrDefault("groupMatch", 5);
            log.info("👨‍🎓 Совпадение группы: {} (ментор) и {} (менти)", mentor.getId(), mentee.getId());
        } else {
            log.info("❌ Отклонение: {} (ментор) и {} (менти) — разные направления", mentor.getId(), mentee.getId());
            return -1;
        }

        Set<DayOfWeek> commonDays = new HashSet<>(mentor.getAvailableDays());
        commonDays.retainAll(mentee.getAvailableDays());
        if (!commonDays.isEmpty()) {
            score += weights.getOrDefault("availability", 8);
            log.info("📅 Общие дни доступности: {} (ментор) и {} (менти) → {}", mentor.getId(), mentee.getId(), commonDays);
        }

        if (mentor.getTimeRange().equals(mentee.getTimeRange())) {
            score += weights.getOrDefault("timeRange", 5);
            log.info("🕒 Совпадение временных предпочтений: {} (ментор) и {} (менти)", mentor.getId(), mentee.getId());
        }

        return score;
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

    public List<ParticipantInProgrammeYear> sortParticipantsByScore(
            ParticipantInProgrammeYear participant,
            List<ParticipantInProgrammeYear> others,
            Map<String, Integer> weights
    ) {
        return others.stream()
                .sorted(Comparator.comparingDouble((ParticipantInProgrammeYear o) -> calculateScore(participant, o, weights)).reversed())
                .toList();
    }
}
