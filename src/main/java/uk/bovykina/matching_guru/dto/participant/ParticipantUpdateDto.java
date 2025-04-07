package uk.bovykina.matching_guru.dto.participant;

import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.*;

import java.time.DayOfWeek;
import java.util.Set;

@Getter
@Setter
public class ParticipantUpdateDto {
    private ParticipantRole role;
    private Integer menteesNumber;
    private Boolean isMatched;
    private AcademicStage academicStage;
    private Boolean hadPlacement;
    private String placementDescription;
    private String motivation;
    private Boolean wasMatchedLastYear;
    private Set<DayOfWeek> availableDays;
    private TimeRange timeRange;
    private MeetingFrequency meetingsFrequency;
    private Set<Skill> skills;
}
