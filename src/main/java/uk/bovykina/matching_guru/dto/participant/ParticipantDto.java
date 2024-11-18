package uk.bovykina.matching_guru.dto.participant;

import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.entity.enums.AcademicStage;

@Getter
@Setter
public class ParticipantDto {
    private Long id;
    private Long userId;
    private Long programmeYearId;
    private ParticipantRole role;
    private Integer menteesNumber;
    private Boolean isMatched;
    private AcademicStage academicStage;
    private Boolean hadPlacement;
    private String placementDescription;
    private String motivation;
    private Boolean isReturningParticipant;
}