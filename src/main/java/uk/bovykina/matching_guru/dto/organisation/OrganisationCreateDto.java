package uk.bovykina.matching_guru.dto.organisation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganisationCreateDto {
    private String name;
    private String description;
    private String logoUrl;
}