package uk.bovykina.matching_guru.algorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.algorithms.helpers.CompatibilityMatrixBuilder;
import uk.bovykina.matching_guru.algorithms.helpers.MatchAssigner;
import uk.bovykina.matching_guru.algorithms.helpers.MatchSaver;
import uk.bovykina.matching_guru.algorithms.helpers.ParticipantLoader;
import uk.bovykina.matching_guru.algorithms.interfaces.MatchingAlgorithm;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.service.ProgrammeYearService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BraceService implements MatchingAlgorithm {

    private final ParticipantLoader participantLoader;
    private final CompatibilityMatrixBuilder matrixBuilder;
    private final MatchAssigner matchAssigner;
    private final MatchSaver matchSaver;
    private final ProgrammeYearService programmeYearService;

    @Override
    public void match(Long programmeYearId) {
        log.info("▶ Running BRACE for ProgrammeYear ID: {}", programmeYearId);

        ProgrammeYear programmeYear = programmeYearService.getById(programmeYearId);
        boolean strictStage = Boolean.TRUE.equals(programmeYear.getStrictAcademicStage());
        boolean strictGroup = Boolean.TRUE.equals(programmeYear.getStrictCourseGroup());

        List<ParticipantInProgrammeYear> mentors = new ArrayList<>(participantLoader.loadMentors(programmeYearId));
        List<ParticipantInProgrammeYear> mentees = new ArrayList<>(participantLoader.loadMentees(programmeYearId));

        if (mentors.isEmpty() || mentees.isEmpty()) {
            log.warn("⚠ Not enough participants for matching");
            return;
        }

        Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> compatibilityScores =
                matrixBuilder.build(mentors, mentees, programmeYearId, strictStage, strictGroup);

        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> matches =
                matchAssigner.assignMatches(mentors, mentees, compatibilityScores, programmeYear);

        matchSaver.save(programmeYearId, matches, compatibilityScores);
        log.info("✔ BRACE completed for ProgrammeYear ID: {}", programmeYearId);
    }
}
