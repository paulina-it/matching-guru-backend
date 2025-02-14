package uk.bovykina.matching_guru.dto.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;

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

    private Long menteeId;
    private String menteeName;
    private String menteeAcademicStage;
    private String menteeCourse;

    private MatchStatus status;
}
