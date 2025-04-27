package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.course.CourseGroupCreateDto;
import uk.bovykina.matching_guru.dto.course.CourseGroupDto;
import uk.bovykina.matching_guru.dto.course.CourseGroupUpdateDto;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.mapper.CourseGroupMapper;
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
    private final CourseGroupMapper courseGroupMapper;

    /**
     * Creates a new course group for a specific organisation.
     */
    public CourseGroupDto createCourseGroup(CourseGroupCreateDto dto) {
        log.info("Creating course group: {}", dto.getName());

        Organisation organisation = organisationRepository.findById(dto.getOrganisationId())
                .orElseThrow(() -> {
                    log.error("Organisation not found: {}", dto.getOrganisationId());
                    return new IllegalArgumentException("Organisation not found with ID: " + dto.getOrganisationId());
                });

        CourseGroup courseGroup = new CourseGroup();
        courseGroup.setName(dto.getName());
        courseGroup.setOrganisation(organisation);

        CourseGroup saved = courseGroupRepository.save(courseGroup);
        return courseGroupMapper.toDto(saved);
    }

    /**
     * Retrieves a course group by its ID.
     */
    public CourseGroupDto getCourseGroupById(Long id) {
        log.info("Fetching course group ID: {}", id);

        CourseGroup courseGroup = courseGroupRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course group not found: {}", id);
                    return new IllegalArgumentException("Course group not found with ID: " + id);
                });

        return courseGroupMapper.toDto(courseGroup);
    }

    /**
     * Retrieves all course groups for an organisation including their courses.
     */
    public List<CourseGroupDto> getCourseGroupsByOrganisationId(Long organisationId) {
        log.info("Fetching course groups for organisation ID: {}", organisationId);

        return courseGroupRepository.findByOrganisationId(organisationId).stream()
                .map(group -> {
                    CourseGroupDto dto = courseGroupMapper.toDto(group);
                    List<Course> courses = courseRepository.findByGroupId(group.getId());
                    dto.setCourses(courses.stream().map(courseGroupMapper::toCourseDto).collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing course group's name.
     */
    public CourseGroupDto updateCourseGroup(Long id, CourseGroupUpdateDto dto) {
        log.info("Updating course group ID: {}", id);

        CourseGroup courseGroup = courseGroupRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course group not found: {}", id);
                    return new IllegalArgumentException("Course group not found with ID: " + id);
                });

        courseGroup.setName(dto.getName());

        CourseGroup updated = courseGroupRepository.save(courseGroup);
        return courseGroupMapper.toDto(updated);
    }

    /**
     * Deletes a course group by its ID.
     */
    public void deleteCourseGroup(Long id) {
        log.info("Deleting course group ID: {}", id);
        courseGroupRepository.deleteById(id);
    }
}
