package uk.bovykina.matching_guru.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.*;

@Setter
@Getter
public class UserCreateDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Email(message = "Invalid university email")
    private String uniEmail;

    @Size(max = 20, message = "Student number must be under 20 characters")
    private String studentNumber;

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotBlank(message = "Join code is required")
    private String joinCode;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Personality type is required")
    private PersonalityType personalityType;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Age is required")
    private Integer age;

    private String inviteToken;

    @Size(max = 50)
    private String ethnicity;

    @Size(max = 50)
    private String nationality;

    @Size(max = 50)
    private String homeCountry;

    @NotNull(message = "Living arrangement is required")
    private LivingArrangement livingArrangement;

    private String disability;

    @Size(max = 255)
    private String profileImageUrl;

    private AgeGroup ageGroup;

    private Long courseId;
}
