package uk.bovykina.matching_guru.dto.user;

import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.UserRole;

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
}
