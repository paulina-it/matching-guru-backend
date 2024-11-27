package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.course.*;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseGroupService {

    private final CourseGroupRepository courseGroupRepository;
    private final CourseRepository courseRepository;
    private final OrganisationRepository organisationRepository;

    public CourseGroupDto createCourseGroup(CourseGroupCreateDto courseGroupCreateDto) {
        Organisation organisation = organisationRepository.findById(courseGroupCreateDto.getOrganisationId())
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with id: " + courseGroupCreateDto.getOrganisationId()));

        CourseGroup courseGroup = new CourseGroup();
        courseGroup.setName(courseGroupCreateDto.getName());
        courseGroup.setOrganisation(organisation);

        CourseGroup savedCourseGroup = courseGroupRepository.save(courseGroup);
        return toCourseGroupDto(savedCourseGroup);
    }

    public List<CourseGroupDto> getCourseGroupsByOrganisationId(Long organisationId) {
        return courseGroupRepository.findByOrganisationId(organisationId).stream()
                .map(courseGroup -> {
                    CourseGroupDto dto = toCourseGroupDto(courseGroup);
                    List<Course> courses = courseRepository.findByGroupId(courseGroup.getId()); // Fetch courses for the group
                    dto.setCourses(courses.stream().map(this::toCourseDto).collect(Collectors.toList())); // Map courses to DTO
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void deleteCourseGroup(Long courseGroupId) {
        courseGroupRepository.deleteById(courseGroupId);
    }

    private CourseGroupDto toCourseGroupDto(CourseGroup courseGroup) {
        CourseGroupDto dto = new CourseGroupDto();
        dto.setId(courseGroup.getId());
        dto.setName(courseGroup.getName());
        dto.setOrganisationId(courseGroup.getOrganisation().getId());
        return dto;
    }

    private CourseDto toCourseDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setType(course.getType());
        dto.setDuration(course.getDuration());
        dto.setGroupId(course.getGroup().getId());
        return dto;
    }
}
