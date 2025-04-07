package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.bovykina.matching_guru.dto.course.CourseCreateDto;
import uk.bovykina.matching_guru.dto.course.CourseDto;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.enums.CourseType;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;
import uk.bovykina.matching_guru.repository.ProgrammeRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class CourseServiceTest {

    @InjectMocks
    private CourseService courseService;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ProgrammeRepository programmeRepository;

    @Mock
    private CourseGroupRepository courseGroupRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCourse() {
        CourseCreateDto createDto = new CourseCreateDto();
        createDto.setName("Computer Science");
        createDto.setType(CourseType.UNDERGRAD);
        createDto.setDuration(3);
        createDto.setGroupId(1L);

        CourseGroup mockGroup = new CourseGroup();
        mockGroup.setId(1L);

        when(courseGroupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));

        Course savedCourse = new Course();
        savedCourse.setId(1L);
        savedCourse.setName("Computer Science");
        savedCourse.setType(CourseType.UNDERGRAD);
        savedCourse.setDuration(3);
        savedCourse.setGroup(mockGroup);

        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        CourseDto result = courseService.createCourse(createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Computer Science", result.getName());
        assertEquals(CourseType.UNDERGRAD, result.getType());
        assertEquals(3, result.getDuration());
        assertEquals(1L, result.getGroupId());

        verify(courseGroupRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void testGetCoursesByGroupId() {
        Long groupId = 1L;

        CourseGroup mockGroup = new CourseGroup();
        mockGroup.setId(groupId);

        Course course1 = new Course();
        course1.setId(1L);
        course1.setName("Course 1");
        course1.setType(CourseType.UNDERGRAD);
        course1.setDuration(3);
        course1.setGroup(mockGroup);

        Course course2 = new Course();
        course2.setId(2L);
        course2.setName("Course 2");
        course2.setType(CourseType.POSTGRAD);
        course2.setDuration(1);
        course2.setGroup(mockGroup);

        when(courseRepository.findByGroupId(groupId)).thenReturn(List.of(course1, course2));

        List<CourseDto> result = courseService.getCoursesByGroupId(groupId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Course 1", result.get(0).getName());
        assertEquals("Course 2", result.get(1).getName());

        verify(courseRepository).findByGroupId(groupId);
    }

    @Test
    void testGetEligibleCoursesByProgrammeId() {
        Long programmeId = 1L;

        Programme mockProgramme = new Programme();
        mockProgramme.setId(programmeId);

        CourseGroup mockGroup1 = new CourseGroup();
        mockGroup1.setId(1L);

        CourseGroup mockGroup2 = new CourseGroup();
        mockGroup2.setId(2L);

        Set<CourseGroup> eligibleGroups = new HashSet<>(Set.of(mockGroup1, mockGroup2));
        mockProgramme.setEligibleCourseGroups(eligibleGroups);

        Course course1 = new Course();
        course1.setId(1L);
        course1.setName("Course 1");
        course1.setType(CourseType.UNDERGRAD);
        course1.setDuration(3);
        course1.setGroup(mockGroup1);

        Course course2 = new Course();
        course2.setId(2L);
        course2.setName("Course 2");
        course2.setType(CourseType.POSTGRAD);
        course2.setDuration(1);
        course2.setGroup(mockGroup2);

        when(programmeRepository.findById(programmeId)).thenReturn(Optional.of(mockProgramme));
        when(courseRepository.findByGroupIds(argThat(ids ->
                ids.contains(1L) && ids.contains(2L) && ids.size() == 2
        ))).thenReturn(Arrays.asList(course1, course2));

        List<CourseDto> result = courseService.getEligibleCoursesByProgrammeId(programmeId);

        assertNotNull(result);
        assertEquals(2, result.size(), "There should be 2 eligible courses");
        assertEquals("Course 1", result.get(0).getName());
        assertEquals("Course 2", result.get(1).getName());

        verify(programmeRepository).findById(programmeId);
        verify(courseRepository).findByGroupIds(anyList());
    }

    @Test
    void testDeleteCourse() {
        Long courseId = 1L;
        courseService.deleteCourse(courseId);
        verify(courseRepository).deleteById(courseId);
    }
}
