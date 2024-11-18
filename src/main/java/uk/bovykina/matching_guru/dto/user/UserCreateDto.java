package uk.bovykina.matching_guru.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.UserRole;

@Getter
public class UserCreateDto {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @Email
    @Setter
    private String uniEmail;

    private Integer studentNumber;

    @NotNull
    private UserRole role;

    private String joinCode;
    @NotNull
    private String password;
}
