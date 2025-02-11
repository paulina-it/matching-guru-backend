package uk.bovykina.matching_guru.dto.participant;

import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.entity.enums.AcademicStage;
import uk.bovykina.matching_guru.entity.enums.Skill;
import uk.bovykina.matching_guru.entity.enums.TimeRange;

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
    private Boolean isReturningParticipant;
    private Set<DayOfWeek> availableDays;
    private TimeRange timeRange;
    private Set<Skill> skills;
}