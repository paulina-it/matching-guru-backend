package uk.bovykina.matching_guru.dto.programme;

import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.AcademicStage;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;

@Data
public class EligibilityDto {
    private Long id;
    private Long programmeId;
    private AcademicStage academicStage;
    private ParticipantRole role;

}