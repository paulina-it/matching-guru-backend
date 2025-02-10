package uk.bovykina.matching_guru.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;

@Getter
@Setter
@AllArgsConstructor
public class UserParticipationDto {
    private String programmeYear;
    private ParticipantRole role;
}
