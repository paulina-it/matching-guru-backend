package uk.bovykina.matching_guru.dto.programme;


import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class ProgrammeDto {
    private Long id;
    private String name;
    private String description;
    private String contactEmail;
    private Long organisationId;
    private Set<Long> courseGroupIds;
    private Map<Long, String> courseGroups;
    private Integer participants;
}