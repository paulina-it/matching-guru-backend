package uk.bovykina.matching_guru.dto.course;

import lombok.Data;

@Data
public class CourseGroupCreateDto {
    private String name;
    private Long organisationId;
}
