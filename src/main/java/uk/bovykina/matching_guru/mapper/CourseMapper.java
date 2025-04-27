package uk.bovykina.matching_guru.mapper;

import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.dto.course.CourseDto;
import uk.bovykina.matching_guru.entity.Course;

@Component
public class CourseMapper {

    public CourseDto toDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setType(course.getType());
        dto.setDuration(course.getDuration());
        dto.setGroupId(course.getGroup().getId());
        return dto;
    }
}