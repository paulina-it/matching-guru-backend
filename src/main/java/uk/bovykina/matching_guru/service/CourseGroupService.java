package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CourseGroupService {

    private final CourseGroupRepository courseGroupRepository;
    private final CourseRepository courseRepository;
    private final OrganisationRepository organisationRepository;

    /**
     * Create a new course group.
     */
    public CourseGroupDto createCourseGroup(CourseGroupCreateDto courseGroupCreateDto) {
        log.info("🔄 Creating a new course group: {}", courseGroupCreateDto.getName());
        Organisation organisation = organisationRepository.findById(courseGroupCreateDto.getOrganisationId())
                .orElseThrow(() -> {
                    log.error("❌ Organisation not found: {}", courseGroupCreateDto.getOrganisationId());
                    return new IllegalArgumentException("Organisation not found with ID: " + courseGroupCreateDto.getOrganisationId());
                });

        CourseGroup courseGroup = new CourseGroup();
        courseGroup.setName(courseGroupCreateDto.getName());
        courseGroup.setOrganisation(organisation);

        CourseGroup savedCourseGroup = courseGroupRepository.save(courseGroup);
        log.info("✅ Course group created successfully: {}", savedCourseGroup.getId());
        return toCourseGroupDto(savedCourseGroup);
    }

    /**
     * Retrieve a course group by ID.
     */
    public CourseGroupDto getCourseGroupById(Long courseGroupId) {
        log.info("🔄 Fetching course group ID: {}", courseGroupId);
        CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> {
                    log.error("❌ Course group not found: {}", courseGroupId);
                    return new IllegalArgumentException("Course group not found with ID: " + courseGroupId);
                });

        log.info("✅ Course group found: {}", courseGroup.getName());
        return toCourseGroupDto(courseGroup);
    }

    /**
     * Get course groups by organisation ID.
     */
    public List<CourseGroupDto> getCourseGroupsByOrganisationId(Long organisationId) {
        log.info("🔄 Fetching course groups for organisation ID: {}", organisationId);
        return courseGroupRepository.findByOrganisationId(organisationId).stream()
                .map(courseGroup -> {
                    CourseGroupDto dto = toCourseGroupDto(courseGroup);
                    List<Course> courses = courseRepository.findByGroupId(courseGroup.getId());
                    dto.setCourses(courses.stream().map(this::toCourseDto).collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Update an existing course group.
     */
    public CourseGroupDto updateCourseGroup(Long courseGroupId, CourseGroupUpdateDto courseGroupUpdateDto) {
        log.info("🔄 Updating course group ID: {}", courseGroupId);
        CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> {
                    log.error("❌ Course group not found: {}", courseGroupId);
                    return new IllegalArgumentException("Course group not found with ID: " + courseGroupId);
                });

        courseGroup.setName(courseGroupUpdateDto.getName());

        CourseGroup updatedCourseGroup = courseGroupRepository.save(courseGroup);
        log.info("✅ Course group updated successfully: {}", updatedCourseGroup.getId());
        return toCourseGroupDto(updatedCourseGroup);
    }

    /**
     * Delete a course group by ID.
     */
    public void deleteCourseGroup(Long courseGroupId) {
        log.info("🔄 Deleting course group ID: {}", courseGroupId);
        courseGroupRepository.deleteById(courseGroupId);
        log.info("✅ Course group deleted: {}", courseGroupId);
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
