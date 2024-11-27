package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.course.*;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
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
