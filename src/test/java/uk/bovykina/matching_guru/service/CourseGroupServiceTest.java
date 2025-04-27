package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.bovykina.matching_guru.dto.course.CourseDto;
import uk.bovykina.matching_guru.dto.course.CourseGroupCreateDto;
import uk.bovykina.matching_guru.dto.course.CourseGroupDto;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.enums.CourseType;
import uk.bovykina.matching_guru.mapper.CourseGroupMapper;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseGroupServiceTest {

    @Mock
    private CourseGroupRepository courseGroupRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private CourseGroupMapper courseGroupMapper;

    @InjectMocks
    private CourseGroupService courseGroupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCourseGroup() {
        Long organisationId = 1L;
        Organisation organisation = new Organisation();
        organisation.setId(organisationId);

        CourseGroupCreateDto courseGroupCreateDto = new CourseGroupCreateDto();
        courseGroupCreateDto.setName("Test Group");
        courseGroupCreateDto.setOrganisationId(organisationId);

        CourseGroup savedGroup = new CourseGroup();
        savedGroup.setId(1L);
        savedGroup.setName("Test Group");
        savedGroup.setOrganisation(organisation);

        CourseGroupDto expectedDto = new CourseGroupDto();
        expectedDto.setId(1L);
        expectedDto.setName("Test Group");
        expectedDto.setOrganisationId(organisationId);

        when(organisationRepository.findById(organisationId)).thenReturn(Optional.of(organisation));
        when(courseGroupRepository.save(any(CourseGroup.class))).thenReturn(savedGroup);
        when(courseGroupMapper.toDto(savedGroup)).thenReturn(expectedDto);

        CourseGroupDto result = courseGroupService.createCourseGroup(courseGroupCreateDto);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertEquals(expectedDto.getName(), result.getName());
        assertEquals(expectedDto.getOrganisationId(), result.getOrganisationId());

        verify(organisationRepository).findById(organisationId);
        verify(courseGroupRepository).save(any(CourseGroup.class));
        verify(courseGroupMapper).toDto(savedGroup);
    }

    @Test
    void testGetCourseGroupsByOrganisationId() {
        Long organisationId = 1L;

        Organisation organisation = new Organisation();
        organisation.setId(organisationId);

        CourseGroup courseGroup = new CourseGroup();
        courseGroup.setId(1L);
        courseGroup.setName("Test Group");
        courseGroup.setOrganisation(organisation);

        Course course = new Course();
        course.setId(1L);
        course.setName("Test Course");
        course.setType(CourseType.UNDERGRAD);
        course.setDuration(3);
        course.setGroup(courseGroup);

        CourseDto courseDto = new CourseDto();
        courseDto.setId(1L);
        courseDto.setName("Test Course");
        courseDto.setType(CourseType.UNDERGRAD);
        courseDto.setDuration(3);

        CourseGroupDto courseGroupDto = new CourseGroupDto();
        courseGroupDto.setId(1L);
        courseGroupDto.setName("Test Group");
        courseGroupDto.setOrganisationId(organisationId);
        courseGroupDto.setCourses(List.of(courseDto));

        when(courseGroupRepository.findByOrganisationId(organisationId)).thenReturn(List.of(courseGroup));
        when(courseRepository.findByGroupId(courseGroup.getId())).thenReturn(List.of(course));
        when(courseGroupMapper.toDto(courseGroup)).thenReturn(courseGroupDto);
        when(courseGroupMapper.toCourseDto(course)).thenReturn(courseDto);

        List<CourseGroupDto> result = courseGroupService.getCourseGroupsByOrganisationId(organisationId);

        assertNotNull(result);
        assertEquals(1, result.size());
        CourseGroupDto actualDto = result.get(0);
        assertEquals(courseGroup.getId(), actualDto.getId());
        assertEquals("Test Group", actualDto.getName());
        assertEquals(1, actualDto.getCourses().size());
        assertEquals(courseDto.getId(), actualDto.getCourses().get(0).getId());

        verify(courseGroupRepository).findByOrganisationId(organisationId);
        verify(courseRepository).findByGroupId(courseGroup.getId());
        verify(courseGroupMapper).toDto(courseGroup);
    }

    @Test
    void testDeleteCourseGroup() {
        Long courseGroupId = 1L;
        courseGroupService.deleteCourseGroup(courseGroupId);
        verify(courseGroupRepository).deleteById(courseGroupId);
    }

    @Test
    void testCreateCourseGroup_OrganisationNotFound() {
        Long organisationId = 1L;
        CourseGroupCreateDto courseGroupCreateDto = new CourseGroupCreateDto();
        courseGroupCreateDto.setName("Test Group");
        courseGroupCreateDto.setOrganisationId(organisationId);

        when(organisationRepository.findById(organisationId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> courseGroupService.createCourseGroup(courseGroupCreateDto)
        );

        assertEquals("Organisation not found with ID: " + organisationId, exception.getMessage());
        verify(organisationRepository).findById(organisationId);
        verifyNoInteractions(courseGroupRepository);
    }
}
