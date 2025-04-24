package uk.bovykina.matching_guru.algorithms.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.AcademicStage;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.repository.MatchRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class MentorshipValidator {
    private final MatchRepository matchRepository;

    private static final Map<AcademicStage, List<AcademicStage>> validPairs = Map.of(
            AcademicStage.FOUNDATION, List.of(AcademicStage.FIRST_YEAR),
            AcademicStage.FIRST_YEAR, List.of(AcademicStage.SECOND_YEAR),
            AcademicStage.SECOND_YEAR, List.of(AcademicStage.PLACEMENT, AcademicStage.FINAL_YEAR),
            AcademicStage.FINAL_YEAR, List.of(AcademicStage.PG_MASTERS, AcademicStage.PG_PHD)
    );

    public boolean isValid(AcademicStage mentorStage, AcademicStage menteeStage) {
        return validPairs.getOrDefault(menteeStage, List.of()).contains(mentorStage);
    }

    public boolean isFallbackValid(AcademicStage mentorStage, AcademicStage menteeStage) {
        return mentorStage.getLevel() > menteeStage.getLevel();
    }

    public boolean isCompatible(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee,
                                boolean strictStage, boolean strictCourseGroup) {
        boolean validStage = strictStage
                ? isValid(mentor.getAcademicStage(), mentee.getAcademicStage())
                : isValid(mentor.getAcademicStage(), mentee.getAcademicStage()) ||
                isFallbackValid(mentor.getAcademicStage(), mentee.getAcademicStage());

        if (!validStage) {
            return false;
        }

        if (mentor.getCourse() == null || mentee.getCourse() == null) {
            return false;
        }

        if (strictCourseGroup) {
            Long mentorGroupId = mentor.getCourseGroup() != null ? mentor.getCourseGroup().getId() : null;
            Long menteeGroupId = mentee.getCourseGroup() != null ? mentee.getCourseGroup().getId() : null;

            if (!Objects.equals(mentorGroupId, menteeGroupId)) {
                System.out.println("❌ Skipping pair due to course group mismatch");
                System.out.printf("Mentor %d in group %s, Mentee %d in group %s%n",
                        mentor.getId(), mentorGroupId, mentee.getId(), menteeGroupId);
                return false;
            }
        }
        System.out.println("✅ Valid match: " + mentor.getId() + " + " + mentee.getId());
        return true;
    }

    public boolean isCompatible(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee, boolean strictCourseGroup) {
        return isCompatible(mentor, mentee, true, strictCourseGroup);
    }

    public boolean isCompatibleWithHistoryCheck(ParticipantInProgrammeYear mentor,
                                                ParticipantInProgrammeYear mentee,
                                                boolean strictStage,
                                                boolean strictCourseGroup) {

        if (matchRepository.existsByMentorIdAndMenteeIdAndStatusIn(
                mentor.getId(), mentee.getId(),
                List.of(MatchStatus.DECLINED, MatchStatus.REJECTED))) {

            System.out.printf("🚫 Previously rejected/declined: Mentor %d ↔ Mentee %d%n", mentor.getId(), mentee.getId());
            return false;
        }

        return isCompatible(mentor, mentee, strictStage, strictCourseGroup);
    }

}
