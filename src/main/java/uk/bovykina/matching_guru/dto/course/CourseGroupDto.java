package uk.bovykina.matching_guru.dto.course;

import lombok.Data;
import java.util.List;
@Data
public class CourseGroupDto {
    private Long id;
    private String name;
    private Long organisationId;
    private List<CourseDto> courses;
}
