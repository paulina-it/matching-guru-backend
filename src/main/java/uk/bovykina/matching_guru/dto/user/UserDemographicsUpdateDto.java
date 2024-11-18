package uk.bovykina.matching_guru.dto.user;

import uk.bovykina.matching_guru.entity.enums.Gender;
import lombok.Data;

@Data
public class UserDemographicsUpdateDto {
    private Gender gender;
    private Integer age;
    private String ethnicity;
    private String nationality;
    private String homeCountry;
    private String homeCity;
    private String livingArrangement;
    private String disability;
    private String dbsCertificate;
}