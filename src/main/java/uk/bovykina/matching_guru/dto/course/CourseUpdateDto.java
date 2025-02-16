package uk.bovykina.matching_guru.dto.course;

import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.CourseType;

@Data
public class CourseUpdateDto {
    private String name;
    private CourseType type;
    private Integer duration;
}
