package uk.bovykina.matching_guru.algorithms.helpers;

import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.AcademicStage;

import java.util.List;
import java.util.Map;

@Component
public class MentorshipValidator {
    private static final Map<AcademicStage, List<AcademicStage>> validPairs = Map.of(
            AcademicStage.FOUNDATION, List.of(AcademicStage.FIRST_YEAR),
            AcademicStage.FIRST_YEAR, List.of(AcademicStage.SECOND_YEAR),
            AcademicStage.SECOND_YEAR, List.of(AcademicStage.PLACEMENT, AcademicStage.FINAL_YEAR),
            AcademicStage.FINAL_YEAR, List.of(AcademicStage.PG_MASTERS, AcademicStage.PG_PHD)
    );

    public boolean isValid(AcademicStage mentorStage, AcademicStage menteeStage) {
        return validPairs.getOrDefault(menteeStage, List.of()).contains(mentorStage);
    }

    public boolean isCompatible(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee) {
        if (!isValid(mentor.getAcademicStage(), mentee.getAcademicStage())) {
            return false;
        }

        if (mentor.getCourse() == null || mentee.getCourse() == null) return false;
        if (mentor.getCourse().getId().equals(mentee.getCourse().getId())) return true;

        return mentor.getCourseGroup() != null &&
                mentee.getCourseGroup() != null &&
                mentor.getCourseGroup().getId().equals(mentee.getCourseGroup().getId());
    }

}
