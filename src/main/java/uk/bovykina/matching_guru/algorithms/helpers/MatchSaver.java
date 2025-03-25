package uk.bovykina.matching_guru.algorithms.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.algorithms.interfaces.CompatibilityCalculator;
import uk.bovykina.matching_guru.dto.match.MatchCreateDto;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.service.MatchService;
import uk.bovykina.matching_guru.service.ProgrammeYearService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchSaver {

    private final MatchService matchService;
    private final ParticipantRepository participantRepository;
    private final ProgrammeYearService programmeYearService;
    private final CompatibilityCalculator compatibilityCalculator;

    public void save(Long programmeYearId,
                     Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches,
                     Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> scores) {

        ProgrammeYear programmeYear = programmeYearService.getById(programmeYearId);

        matches.forEach((mentee, mentor) -> {
            double score = scores.getOrDefault(mentor, Map.of()).getOrDefault(mentee, 0.0);

            if (matchService.doesMatchExist(mentor.getId(), mentee.getId())) {
                log.warn("⚠ Match already exists: {} → {}", mentor.getId(), mentee.getId());
                return;
            }

            MatchStatus status = getStatus(score, programmeYear);
            MatchCreateDto dto = new MatchCreateDto(programmeYearId, mentor.getId(), mentee.getId(), score, status);

            try {
                matchService.createMatch(dto);
                mentor.setIsMatched(true);
                mentee.setIsMatched(true);
                participantRepository.saveAll(List.of(mentor, mentee));
                log.info("✅ Match saved: {} → {} | score = {}", mentor.getId(), mentee.getId(), score);
            } catch (Exception e) {
                log.error("❌ Error saving match {} → {}: {}", mentor.getId(), mentee.getId(), e.getMessage());
            }
        });
    }

    public void saveMatches(
            Long programmeYearId,
            ProgrammeYear programmeYear,
            Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> matches,
            Map<String, Integer> weights
    ) {
        matches.forEach((mentor, mentees) -> {
            for (ParticipantInProgrammeYear mentee : mentees) {
                double score = compatibilityCalculator.calculate(mentor, mentee, weights);

                if (matchService.doesMatchExist(mentor.getId(), mentee.getId())) {
                    log.warn("⚠ Match already exists: {} → {}", mentor.getId(), mentee.getId());
                    continue;
                }

                MatchStatus status = getStatus(score, programmeYear);
                MatchCreateDto dto = new MatchCreateDto(programmeYearId, mentor.getId(), mentee.getId(), score, status);

                try {
                    matchService.createMatch(dto);
                    mentor.setIsMatched(true);
                    mentee.setIsMatched(true);
                    participantRepository.saveAll(List.of(mentor, mentee));
                    log.info("✅ Match saved: {} → {} | score = {}", mentor.getId(), mentee.getId(), score);
                } catch (Exception e) {
                    log.error("❌ Error saving match {} → {}: {}", mentor.getId(), mentee.getId(), e.getMessage());
                }
            }
        });
    }


    private MatchStatus getStatus(double score, ProgrammeYear programmeYear) {
        return switch (programmeYear.getMatchApprovalType()) {
            case AUTO -> MatchStatus.APPROVED;
            case MANUAL -> MatchStatus.PENDING;
            case THRESHOLD -> score < programmeYear.getApprovalThreshold() ? MatchStatus.PENDING : MatchStatus.APPROVED;
        };
    }
}
