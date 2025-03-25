package uk.bovykina.matching_guru.dto.user;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import uk.bovykina.matching_guru.entity.enums.*;

@Getter
public class UserUpdateDto {
    private Long id;
    private String firstName;
    private String lastName;

    @Email
    private String email;

    @Email
    private String uniEmail;

    private String studentNumber;
    private UserRole role;
    private Long organisationId;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private PersonalityType personalityType;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Gender gender;
    private String ethnicity;
    private String nationality;
    private String homeCountry;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private LivingArrangement livingArrangement;
    private String disability;
    private String profileImageUrl;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private AgeGroup ageGroup;
}
