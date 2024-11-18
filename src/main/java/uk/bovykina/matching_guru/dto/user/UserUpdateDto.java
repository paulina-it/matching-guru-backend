package uk.bovykina.matching_guru.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import uk.bovykina.matching_guru.entity.enums.UserRole;

@Getter
public class UserUpdateDto {
    private Long id;
    private String firstName;
    private String lastName;
    @Email
    private String email;
    @Email
    private String uniEmail;
    private Integer studentNumber;
    private UserRole role;
    private Long organisationId;
}
