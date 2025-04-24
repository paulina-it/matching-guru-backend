package uk.bovykina.matching_guru.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.UserRole;

@Data
@AllArgsConstructor
public class UserSummaryDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
}
