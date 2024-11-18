package uk.bovykina.matching_guru.dto.organisation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganisationDto {
    private Long id;
    private String name;
    private String joinCode;
    private String description;
}