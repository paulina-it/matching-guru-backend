package uk.bovykina.matching_guru.dto.programme;


import lombok.Data;

import java.util.Set;

@Data
public class ProgrammeDto {
    private Long id;
    private String name;
    private String description;
    private Long organisationId;
    private Set<Long> courseGroupIds;
    private Integer participants;
}