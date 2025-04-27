package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import uk.bovykina.matching_guru.dto.course.CourseCreateDto;
import uk.bovykina.matching_guru.dto.course.CourseDto;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.enums.CourseType;
import uk.bovykina.matching_guru.mapper.CourseMapper;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.ProgrammeRepository;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private MultipartFile mockFile;

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

        CourseGroup group = new CourseGroup();
        group.setId(1L);

        Course course = new Course();
        course.setId(1L);
        course.setName("Computer Science");
        course.setGroup(group);
        course.setType(CourseType.UNDERGRAD);
        course.setDuration(3);

        CourseDto expectedDto = new CourseDto();
        expectedDto.setId(1L);
        expectedDto.setName("Computer Science");
        expectedDto.setGroupId(1L);
        expectedDto.setType(CourseType.UNDERGRAD);
        expectedDto.setDuration(3);

        when(courseGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(courseMapper.toDto(course)).thenReturn(expectedDto);

        CourseDto result = courseService.createCourse(createDto);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void testGetCoursesByGroupId() {
        CourseGroup group = new CourseGroup();
        group.setId(1L);

        Course course1 = new Course();
        course1.setId(1L);
        course1.setName("Course 1");
        course1.setGroup(group);

        Course course2 = new Course();
        course2.setId(2L);
        course2.setName("Course 2");
        course2.setGroup(group);

        when(courseRepository.findByGroupId(1L)).thenReturn(List.of(course1, course2));

        CourseDto dto1 = new CourseDto();
        dto1.setId(1L);
        dto1.setName("Course 1");

        CourseDto dto2 = new CourseDto();
        dto2.setId(2L);
        dto2.setName("Course 2");

        when(courseMapper.toDto(course1)).thenReturn(dto1);
        when(courseMapper.toDto(course2)).thenReturn(dto2);

        List<CourseDto> result = courseService.getCoursesByGroupId(1L);

        assertEquals(2, result.size());
        assertEquals("Course 1", result.get(0).getName());
    }

    @Test
    void testGetEligibleCoursesByProgrammeId() {
        Programme programme = new Programme();
        programme.setId(1L);

        CourseGroup group1 = new CourseGroup();
        group1.setId(1L);
        CourseGroup group2 = new CourseGroup();
        group2.setId(2L);
        programme.setEligibleCourseGroups(new HashSet<>(Set.of(group1, group2)));

        Course course1 = new Course();
        course1.setId(1L);
        course1.setGroup(group1);
        course1.setName("Course A");

        Course course2 = new Course();
        course2.setId(2L);
        course2.setGroup(group2);
        course2.setName("Course B");

        when(programmeRepository.findById(1L)).thenReturn(Optional.of(programme));
        when(courseRepository.findByGroupIds(anyList())).thenReturn(List.of(course1, course2));

        CourseDto dto1 = new CourseDto();
        dto1.setId(1L);
        dto1.setName("Course A");
        CourseDto dto2 = new CourseDto();
        dto2.setId(2L);
        dto2.setName("Course B");

        when(courseMapper.toDto(course1)).thenReturn(dto1);
        when(courseMapper.toDto(course2)).thenReturn(dto2);

        List<CourseDto> result = courseService.getEligibleCoursesByProgrammeId(1L);

        assertEquals(2, result.size());
    }

    @Test
    void testDeleteCourse() {
        courseService.deleteCourse(1L);
        verify(courseRepository).deleteById(1L);
    }

    @Test
    void testProcessFile_UnsupportedFormat() {
        when(mockFile.getOriginalFilename()).thenReturn("file.txt");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.processFile(mockFile, 1L);
        });

        assertEquals("Unsupported file type", exception.getMessage());
    }
}
