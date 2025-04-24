package uk.bovykina.matching_guru.dto.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.entity.enums.UserRole;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchResponseDto {
    private Long id;
    private Long programmeYearId;

    private Long mentorId;
    private String mentorName;
    private String mentorAcademicStage;
    private String mentorCourse;
    private double compatibilityScore;
    private String rejectionReason;
    private Long editedByUserId;
    private String editedByUserName;
    private UserRole editedByRole;

    private Long menteeId;
    private String menteeName;
    private String menteeAcademicStage;
    private String menteeCourse;

    private MatchStatus status;
}
