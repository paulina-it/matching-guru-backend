package uk.bovykina.matching_guru.dto.participant;

import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.*;

import java.time.DayOfWeek;
import java.util.Set;

@Getter
@Setter
public class ParticipantResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Gender userGender;
    private Integer userAge;
    private String userNationality;
    private String userHomeCountry;
    private Long programmeYearId;
    private String programmeName;
    private String academicYear;
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
