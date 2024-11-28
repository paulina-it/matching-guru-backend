package uk.bovykina.matching_guru.dto.programme;


import lombok.Data;

import java.util.Set;

@Data
public class ProgrammeUpdateDto {
    private String name;
    private String description;
    private Set<Long> courseGroupIds;
}