package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.match.MatchCreateDto;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeMatchingCriteria;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchApprovalType;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.ProgrammeMatchingCriteriaRepository;
import uk.bovykina.matching_guru.service.MatchService;
import uk.bovykina.matching_guru.service.ProgrammeYearService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BraceService {

    private static final int MAX_MENTEES_PER_MENTOR = 3;

    private final ParticipantRepository participantRepository;
    private final ProgrammeMatchingCriteriaRepository criteriaRepository;
    private final MatchService matchService;
    private final CompatibilityService compatibilityService;
    private final ProgrammeYearService programmeYearService;

    @Transactional
    public void matchParticipantsWithBrace(Long programmeYearId) {
        log.info("▶ Starting BRACE matching process for ProgrammeYear ID: {}", programmeYearId);

        List<ParticipantInProgrammeYear> mentors = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTOR);
        List<ParticipantInProgrammeYear> mentees = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTEE);

        log.info("👥 Found {} mentors and {} mentees", mentors.size(), mentees.size());

        if (mentors.isEmpty() || mentees.isEmpty()) {
            log.warn("⚠ Not enough participants for matching");
            return;
        }

        // Load criteria weights from repository
        Map<String, Integer> weights = loadMatchingCriteria(programmeYearId);
        log.info("📊 Loaded criteria weights: {}", weights);

        // Create a map to store mentor-mentee matches
        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches = new HashMap<>();
        Map<ParticipantInProgrammeYear, Integer> mentorAssignmentCount = new HashMap<>();

        Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> compatibilityScores = new HashMap<>();
        log.debug("🔍 Calculating compatibility scores for mentors and mentees...");
        for (ParticipantInProgrammeYear mentor : mentors) {
            Map<ParticipantInProgrammeYear, Double> mentorScores = new HashMap<>();
            for (ParticipantInProgrammeYear mentee : mentees) {
                double score = compatibilityService.calculateScore(mentor, mentee, weights);
                if (score > 0) {  // ⚠ Ignore invalid matches with score 0
                    mentorScores.put(mentee, score);
                }
            }
            compatibilityScores.put(mentor, mentorScores);
            mentorAssignmentCount.put(mentor, 0); // Track mentee count
        }

        boolean changesMade;
        log.debug("🔄 Starting BRACE algorithm...");
        do {
            changesMade = false;

            for (ParticipantInProgrammeYear mentee : mentees) {
                ParticipantInProgrammeYear bestMentor = findBestMentorForMentee(mentee, mentors, compatibilityScores, matches, mentorAssignmentCount);

                if (bestMentor != null) {
                    matches.put(mentee, bestMentor);
                    mentorAssignmentCount.put(bestMentor, mentorAssignmentCount.get(bestMentor) + 1);
                    changesMade = true;
                    log.info("🔗 Matched Mentee {} with Mentor {} based on compatibility score", mentee.getId(), bestMentor.getId());
                } else {
                    log.warn("⚠ No valid mentor found for Mentee {}", mentee.getId());  // ⚠ Handle unmatched mentees
                }
            }
        } while (changesMade);

        // Save the matches
        log.info("💾 Total matches found: {}", matches.size());
        if (matches.isEmpty()) {
            log.warn("⚠ No matches were saved!");
            return;
        }

        matches.forEach((mentee, mentor) -> {
            double compatibilityScore = compatibilityScores.getOrDefault(mentor, Collections.emptyMap()).getOrDefault(mentee, 0.0);
            if (matchService.doesMatchExist(mentor.getId(), mentee.getId())) {
                log.warn("⚠ Match already exists: {} → {}", mentor.getId(), mentee.getId());
                return;
            }

            MatchStatus defaultStatus = determineApprovalType(programmeYearId, compatibilityScore);
            MatchCreateDto matchCreateDto = new MatchCreateDto(programmeYearId, mentor.getId(), mentee.getId(), compatibilityScore, defaultStatus);

            try {
                matchService.createMatch(matchCreateDto);
                mentor.setIsMatched(true);
                mentee.setIsMatched(true);
                participantRepository.save(mentor);
                participantRepository.save(mentee);
                log.info("✅ Match saved: {} (mentor) → {} (mentee) with compatibility {}", mentor.getId(), mentee.getId(), compatibilityScore);
            } catch (Exception e) {
                log.error("❌ Failed to save match: Mentor {} → Mentee {} | Error: {}", mentor.getId(), mentee.getId(), e.getMessage());
            }
        });

        log.info("✔ BRACE matching process completed for ProgrammeYear ID: {}", programmeYearId);
    }


    private ParticipantInProgrammeYear findBestMentorForMentee(
            ParticipantInProgrammeYear mentee,
            List<ParticipantInProgrammeYear> mentors,
            Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> compatibilityScores,
            Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> currentMatches,
            Map<ParticipantInProgrammeYear, Integer> mentorAssignmentCount) {  // 🛠 Fix: Ensure this parameter exists

        ParticipantInProgrammeYear bestMentor = null;
        double bestScore = 0;

        for (ParticipantInProgrammeYear mentor : mentors) {
            if (mentorAssignmentCount.get(mentor) >= 3) {
                continue;
            }

            double score = compatibilityScores.getOrDefault(mentor, Collections.emptyMap()).getOrDefault(mentee, 0.0);
            if (score > bestScore) {
                bestMentor = mentor;
                bestScore = score;
            }
        }

        return bestMentor;
    }


    private void saveMatch(Long programmeYearId, ParticipantInProgrammeYear mentee, ParticipantInProgrammeYear mentor, double compatibilityScore) {
        if (compatibilityScore <= 0) {
            log.warn("⚠ Invalid match ignored: Mentor {} → Mentee {} with score {}", mentor.getId(), mentee.getId(), compatibilityScore);
            return;
        }

        MatchStatus defaultStatus = determineApprovalType(programmeYearId, compatibilityScore);
        MatchCreateDto matchCreateDto = new MatchCreateDto(programmeYearId, mentor.getId(), mentee.getId(), compatibilityScore, defaultStatus);

        try {
            matchService.createMatch(matchCreateDto);
            mentor.setIsMatched(true);
            mentee.setIsMatched(true);
            participantRepository.save(mentor);
            participantRepository.save(mentee);
            log.info("✅ Match saved: {} (mentor) → {} (mentee) with compatibility {}", mentor.getId(), mentee.getId(), compatibilityScore);
        } catch (Exception e) {
            log.error("❌ Failed to save match: Mentor {} → Mentee {} | Error: {}", mentor.getId(), mentee.getId(), e.getMessage());
        }
    }

    private Map<String, Integer> loadMatchingCriteria(Long programmeYearId) {
        log.debug("🔄 Loading matching criteria for ProgrammeYear ID: {}", programmeYearId);
        return criteriaRepository.findByProgrammeYearId(programmeYearId).stream()
                .collect(Collectors.toMap(
                        c -> c.getCriterionType().name(),
                        ProgrammeMatchingCriteria::getWeight
                ));
    }

    private MatchStatus determineApprovalType(Long programmeYearId, double compatibilityScore) {
        ProgrammeYear programmeYear = programmeYearService.getById(programmeYearId);

        if (programmeYear.getMatchApprovalType() == MatchApprovalType.AUTO) {
            log.debug("🔒 Match status set to APPROVED (AUTO) for compatibility score {}", compatibilityScore);
            return MatchStatus.APPROVED;
        }

        if (programmeYear.getMatchApprovalType() == MatchApprovalType.MANUAL) {
            log.debug("🔒 Match status set to PENDING (MANUAL) for compatibility score {}", compatibilityScore);
            return MatchStatus.PENDING;
        }

        if (programmeYear.getMatchApprovalType() == MatchApprovalType.THRESHOLD) {
            if (compatibilityScore < programmeYear.getApprovalThreshold()) {
                log.debug("🔒 Match status set to PENDING (THRESHOLD) for compatibility score {}", compatibilityScore);
                return MatchStatus.PENDING;
            } else {
                log.debug("🔒 Match status set to APPROVED (THRESHOLD) for compatibility score {}", compatibilityScore);
                return MatchStatus.APPROVED;
            }
        }

        log.debug("🔒 Default match status set to PENDING");
        return MatchStatus.PENDING;
    }
}
