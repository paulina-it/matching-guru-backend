package uk.bovykina.matching_guru.mapper;

import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.dto.course.CourseDto;
import uk.bovykina.matching_guru.dto.course.CourseGroupDto;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.CourseGroup;

@Component
public class CourseGroupMapper {

    public CourseGroupDto toDto(CourseGroup group) {
        CourseGroupDto dto = new CourseGroupDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setOrganisationId(group.getOrganisation().getId());
        return dto;
    }

    public CourseDto toCourseDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setType(course.getType());
        dto.setDuration(course.getDuration());
        dto.setGroupId(course.getGroup().getId());
        return dto;
    }
}
