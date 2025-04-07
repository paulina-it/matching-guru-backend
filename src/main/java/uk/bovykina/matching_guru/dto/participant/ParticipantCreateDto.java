package uk.bovykina.matching_guru.dto.participant;

import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.enums.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.util.Set;

@Getter
@Setter
public class ParticipantCreateDto {
    @NotNull(message = "User ID cannot be null")
    private Long userId;
    @NotNull(message = "Programme Year ID cannot be null")
    private Long programmeYearId;
    @NotNull(message = "Role cannot be null")
    private ParticipantRole role;
    private Integer menteesNumber;
    private Boolean isMatched;
    @NotNull(message = "Academic Stage cannot be null")
    private AcademicStage academicStage;
    private Boolean hadPlacement;
    @Size(max = 500, message = "Placement description must not exceed 500 characters")
    private String placementDescription;
    private String motivation;
    private Boolean wasMatchedLastYear;
    private Set<DayOfWeek> availableDays;
    private TimeRange timeRange;
    private MeetingFrequency meetingsFrequency;
    private Set<Skill> skills;

//    User-related
    private Gender gender;
    private Integer age;
    private String nationality;
    private String homeCountry;
    private PersonalityType personalityType;
    private Long courseId;
    private LivingArrangement livingArrangement;
    private String disability;
    private Boolean dbsCertificate;
    private AgeGroup ageGroup;
}
