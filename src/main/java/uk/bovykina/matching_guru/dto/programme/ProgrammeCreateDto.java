package uk.bovykina.matching_guru.dto.programme;


import lombok.Data;

@Data
public class ProgrammeCreateDto {
    private String name;
    private String description;
    private Long organisationId;
}