package uk.bovykina.matching_guru.dto.participant;

import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.entity.enums.AcademicStage;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
}
