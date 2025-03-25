package uk.bovykina.matching_guru.algorithms.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.algorithms.interfaces.CompatibilityCalculator;
import uk.bovykina.matching_guru.dto.match.MatchCreateDto;
import uk.bovykina.matching_guru.dto.match.MatchResponseDto;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.service.MatchService;
import uk.bovykina.matching_guru.service.ProgrammeYearService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchSaver {

    private final MatchService matchService;
    private final ParticipantRepository participantRepository;
    private final ProgrammeYearService programmeYearService;
    private final CompatibilityCalculator compatibilityCalculator;
    private final MentorshipValidator mentorshipValidator;

    public void save(Long programmeYearId,
                     Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches,
                     Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> scores) {

        ProgrammeYear programmeYear = programmeYearService.getById(programmeYearId);
        AtomicInteger savedCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);

        matches.forEach((mentee, mentor) -> {
            double score = scores.getOrDefault(mentor, Map.of()).getOrDefault(mentee, 0.0);

            if (matchService.doesMatchExist(mentor.getId(), mentee.getId())) {
                log.warn("⚠ Match already exists: {} → {}", mentor.getId(), mentee.getId());
                skippedCount.incrementAndGet();
                return;
            }

            MatchStatus status = getStatus(mentor, mentee, score, programmeYear);
            MatchCreateDto dto = new MatchCreateDto(programmeYearId, mentor.getId(), mentee.getId(), score, status);

            try {
                MatchResponseDto result = matchService.createMatch(dto);
                if (result != null) {
                    savedCount.incrementAndGet();
                    mentor.setIsMatched(true);
                    mentee.setIsMatched(true);
                    participantRepository.saveAll(List.of(mentor, mentee));
                    log.info("✅ Match saved: {} → {} | score = {}", mentor.getId(), mentee.getId(), score);
                } else {
                    skippedCount.incrementAndGet();
                }
            } catch (Exception e) {
                skippedCount.incrementAndGet();
                log.error("❌ Error saving match {} → {}: {}", mentor.getId(), mentee.getId(), e.getMessage());
            }
        });

        log.info("🧾 Match saving summary: ✅ Saved: {}, ⚠ Skipped: {}", savedCount, skippedCount);
    }

    public void saveMatches(
            Long programmeYearId,
            ProgrammeYear programmeYear,
            Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches,
            Map<String, Integer> weights
    ) {
        AtomicInteger savedCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);

        matches.forEach((mentor, mentees) -> {
            for (ParticipantInProgrammeYear mentee : mentees) {
                double score = compatibilityCalculator.calculate(mentor, mentee, weights);

                if (matchService.doesMatchExist(mentor.getId(), mentee.getId())) {
                    log.warn("⚠ Match already exists: {} → {}", mentor.getId(), mentee.getId());
                    skippedCount.incrementAndGet();
                    continue;
                }

                MatchStatus status = getStatus(mentor, mentee, score, programmeYear);
                MatchCreateDto dto = new MatchCreateDto(programmeYearId, mentor.getId(), mentee.getId(), score, status);

                try {
                    MatchResponseDto result = matchService.createMatch(dto);
                    if (result != null) {
                        savedCount.incrementAndGet();
                        mentor.setIsMatched(true);
                        mentee.setIsMatched(true);
                        participantRepository.saveAll(List.of(mentor, mentee));
                        log.info("✅ Match saved: {} → {} | score = {}", mentor.getId(), mentee.getId(), score);
                    } else {
                        skippedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    skippedCount.incrementAndGet();
                    log.error("❌ Error saving match {} → {}: {}", mentor.getId(), mentee.getId(), e.getMessage());
                }
            }
        });

        log.info("🧾 Match saving summary (Gale-Shapley): ✅ Saved: {}, ⚠ Skipped: {}", savedCount, skippedCount);
    }

    private MatchStatus getStatus(ParticipantInProgrammeYear mentor,
                                  ParticipantInProgrammeYear mentee,
                                  double score,
                                  ProgrammeYear programmeYear) {

        boolean isStrict = mentorshipValidator.isValid(mentor.getAcademicStage(), mentee.getAcademicStage());
        boolean isFallback = mentorshipValidator.isFallbackValid(mentor.getAcademicStage(), mentee.getAcademicStage());

        if (!isStrict && isFallback) {
            log.info("🟡 Fallback academic stage match → forcing PENDING approval");
            return MatchStatus.PENDING;
        }

        return switch (programmeYear.getMatchApprovalType()) {
            case AUTO -> MatchStatus.APPROVED;
            case MANUAL -> MatchStatus.PENDING;
            case THRESHOLD -> score < programmeYear.getApprovalThreshold() ? MatchStatus.PENDING : MatchStatus.APPROVED;
            default -> MatchStatus.PENDING;
        };
    }
}
