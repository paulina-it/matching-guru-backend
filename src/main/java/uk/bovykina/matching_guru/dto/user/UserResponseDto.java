package uk.bovykina.matching_guru.dto.user;

import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.dto.match.DetailedMatchResponseDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantDto;
import uk.bovykina.matching_guru.entity.enums.*;
import java.util.List;

@Setter
@Getter
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String uniEmail;
    private Integer studentNumber;
    private UserRole role;
    private Long organisationId;
    private String organisationName;
    private PersonalityType personalityType;
    private Gender gender;
    private Integer age;
    private String ethnicity;
    private String nationality;
    private String homeCountry;
    private LivingArrangement livingArrangement;
    private String disability;
    private List<UserParticipationDto> participations;
    private String profileImageUrl;
}
