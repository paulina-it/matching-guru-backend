package uk.bovykina.matching_guru.dto.participant;

import lombok.Getter;
import lombok.Setter;
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
    private Boolean isReturningParticipant;

    @NotNull(message = "Available days cannot be null")
    private Set<DayOfWeek> availableDays;

    @NotNull(message = "Time range cannot be null")
    private TimeRange timeRange;

    @NotNull(message = "Skills cannot be null")
    private Set<Skill> skills;
}
