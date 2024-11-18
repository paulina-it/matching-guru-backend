package uk.bovykina.matching_guru.dto.user;

import lombok.Data;
import lombok.Getter;
import uk.bovykina.matching_guru.entity.enums.UserRole;

@Data
@Getter
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String uniEmail;
    private Integer studentNumber;
    private UserRole role;
}
