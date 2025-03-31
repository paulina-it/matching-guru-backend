package uk.bovykina.matching_guru.dto.user;

import lombok.Data;
import lombok.Getter;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.enums.*;

@Data
@Getter
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String uniEmail;
    private String studentNumber;
    private UserRole role;
    private PersonalityType personalityType;
    private Gender gender;
    private Integer age;
    private String ethnicity;
    private String nationality;
    private String homeCountry;
    private LivingArrangement livingArrangement;
    private String disability;
    private String profileImageUrl;
    private AgeGroup ageGroup;
    private Course course;
}
