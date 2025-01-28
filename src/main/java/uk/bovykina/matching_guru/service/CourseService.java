package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.course.CourseCreateDto;
import uk.bovykina.matching_guru.dto.course.CourseDto;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;
import uk.bovykina.matching_guru.repository.ProgrammeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final ProgrammeRepository programmeRepository;
    private final CourseGroupRepository courseGroupRepository;

    public CourseDto createCourse(CourseCreateDto courseCreateDto) {
        CourseGroup group = courseGroupRepository.findById(courseCreateDto.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("CourseGroup not found with id: " + courseCreateDto.getGroupId()));

        Course course = new Course();
        course.setName(courseCreateDto.getName());
        course.setType(courseCreateDto.getType());
        course.setDuration(courseCreateDto.getDuration());
        course.setGroup(group);

        Course savedCourse = courseRepository.save(course);
        return toCourseDto(savedCourse);
    }

    public List<CourseDto> getCoursesByGroupId(Long groupId) {
        return courseRepository.findByGroupId(groupId).stream()
                .map(this::toCourseDto)
                .collect(Collectors.toList());
    }


    public List<CourseDto> getEligibleCoursesByProgrammeId(Long programmeId) {
        Programme programme = programmeRepository.findById(programmeId)
                .orElseThrow(() -> new IllegalArgumentException("Programme not found with id: " + programmeId));

        List<Long> courseGroupIds = programme.getEligibleCourseGroups().stream()
                .map(group -> group.getId())
                .collect(Collectors.toList());

        return courseRepository.findByGroupIds(courseGroupIds).stream()
                .map(this::toCourseDto)
                .collect(Collectors.toList());
    }

    public void deleteCourse(Long courseId) {
        courseRepository.deleteById(courseId);
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
